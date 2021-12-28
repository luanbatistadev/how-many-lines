require(`dotenv/config`);
const serverless = require(`serverless-http`);
const express = require(`express`);
const rateLimit = require(`express-rate-limit`);
const fetch = require(`node-fetch`);
const { default: HowManyLines } = require(`how-many-lines`);
const url = require(`url`);

const app = express();

const { REPOSITORY, REPO_TOKEN, GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET } =
  process.env;

const OPEN_ISSUE_EVENT = "open-issue";

async function dispatchGithubWorkflowEvent(payload) {
  const response = await fetch(`https://api.github.com/user`, {
    headers: {
      authorization: `Token ${payload.token}`,
    },
  });

  if (response.status !== 200) throw response;

  await fetch(`https://api.github.com/repos/${REPOSITORY}/dispatches`, {
    method: `POST`,
    body: JSON.stringify({
      event_type: OPEN_ISSUE_EVENT,
      client_payload: payload,
    }),
    headers: {
      authorization: `Token ${REPO_TOKEN}`,
    },
  });
}

/// Apply rate limit of 2 request / 15 minutes
app.use(
  rateLimit({
    windowMs: 15 * 60 * 100,
    max: 2,
  })
);

/// Parse and limit the request body
app.use(express.json({ limit: `100kb` }));

/// Endpoint that accept requests
/// to trigger the workflow on the repository
app.post(`/dispatches`, async function (req, res) {
  if (!req.body.token) return res.status(400).send(`Missing token`);

  try {
    await dispatchGithubWorkflowEvent({ token: req.body.token });

    return res.sendStatus(200);
  } catch (reponse) {
    if (reponse.status !== 200) return res.status(401).send(`Invalid token!`);
  }
});

/// Endpoint that accept requests to run `HowManyLines` core package
app.post(`/`, async function (req, res) {
  if (!req.body.token) return res.status(400).send(`Missing token`);

  const how = HowManyLines({ token: req.body.token });

  const stats = await how.generateStats();
  const lineCount = how.calcLineCount(stats);

  return res.send(`${lineCount}`);
});

/// Endpoint that handle OAuth redirect
app.get(`/oauth`, async function (req, res) {
  const { code } = req.query;

  if (!code) return res.status(400).send(`Missing OAuth code`);

  const response = await fetch(`https://github.com/login/oauth/access_token`, {
    method: `POST`,
    body: JSON.stringify({
      code,
      client_id: GITHUB_CLIENT_ID,
      client_secret: GITHUB_CLIENT_SECRET,
    }),
    headers: {
      accept: "application/json",
      [`Content-Type`]: "application/json",
    },
  });

  if (response.status !== 200) return res.sendStatus(response.status);

  const { access_token: accessToken } = await response.json();

  try {
    await dispatchGithubWorkflowEvent({ token: accessToken });
  } catch (reponse) {
    if (reponse.status !== 200) return res.status(401).send(`Invalid token!`);
  }

  return res.redirect(`https://github.com/${REPOSITORY}/actions`);
});

/// Serverless handler [refer to details](https://www.serverless.com/blog/serverless-express-rest-api/)
module.exports.handler = serverless(app);
