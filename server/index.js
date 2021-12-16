const serverless = require("serverless-http");
const express = require("express");
const rateLimit = require("express-rate-limit");

const app = express();

/// Apply rate limit of 2 request / 15 minutes
app.use(
  rateLimit({
    windowMs: 15 * 60 * 100,
    max: 2,
  })
);

/// Parse and limit the request body
app.use(express.json({ limit: "100kb" }));

/// Endpoint that accept requests here
/// to trigger the workflow on the repository
app.post("/", async function (req, res) {
  if (!req.body.token) return res.status(400).send(`Missing token`);

  /// TODO: Implement
  /// dispatchGithubWorkflowEvent({ payload: { token: req.body.token } });
});

/// Serverless handler [refer to details](https://www.serverless.com/blog/serverless-express-rest-api/)
module.exports.handler = serverless(app);
