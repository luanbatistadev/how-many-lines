require(`dotenv/config`);
const serverless = require(`serverless-http`);
const express = require(`express`);
const rateLimit = require(`express-rate-limit`);
const fetch = require(`node-fetch`);
const { default: HowManyLines } = require(`how-many-lines`);

const app = express();

const { REPOSITORY, REPO_TOKEN } = process.env;

const OPEN_ISSUE_EVENT = "open-issue";

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

  async function dispatchGithubWorkflowEvent(payload) {
    const response = await fetch(`https://api.github.com/user`, {
      headers: {
        authorization: `Token ${payload.token}`,
      },
    });

    if (response.status !== 200) return res.status(401).send(`Invalid token!`);

    const result = await fetch(
      `https://api.github.com/repos/${REPOSITORY}/dispatches`,
      {
        method: `POST`,
        body: JSON.stringify({
          event_type: OPEN_ISSUE_EVENT,
          client_payload: payload,
        }),
        headers: {
          authorization: `Token ${REPO_TOKEN}`,
        },
      }
    );

    return res.sendStatus(result.status);
  }

  await dispatchGithubWorkflowEvent({ token: req.body.token });
});

/// Endpoint that accept requests to run `HowManyLines` core package
app.post(`/`, async function (req, res) {
  if (!req.body.token) return res.status(400).send(`Missing token`);

  const how = HowManyLines({ token: req.body.token });

  const stats = await how.generateStats();
  const lineCount = how.calcLineCount(stats);

  return res.send(`${lineCount}`);
});

/// Serverless handler [refer to details](https://www.serverless.com/blog/serverless-express-rest-api/)
module.exports.handler = serverless(app);
