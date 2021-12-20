/// CLI implementation of `HowManyLines` API
///
/// Usage `node line-count`
const { default: HowManyLines } = require("how-many-lines");

/// Remember to set the environment variable before running it
const token = process.env.USER_TOKEN;

/// Uses the `HowManyLines` API to generate the `lineCount`
async function fetchLineCount() {
  const howManyLines = HowManyLines({ token });

  const stats = await howManyLines.generateStats();

  const lineCount = howManyLines.calcLineCount(stats);

  return lineCount;
}

(async () => process.stdout.write(`${await fetchLineCount()}`))();
