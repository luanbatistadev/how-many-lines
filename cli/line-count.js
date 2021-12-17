/// CLI implementation of `HowManyLines` API
const { default: HowManyLines } = require("how-many-lines");

/// Usage `node line-count TOKEN=<TOKEN>`
const args = process.argv
  .slice(2)
  .map((arg) => arg.split("="))
  .reduce((current, pair) => ({ ...current, [pair[0]]: pair[1] }), {});

const token = args.TOKEN;

/// Uses the `HowManyLines` API to generate the `lineCount`
async function fetchLineCount() {
  const howManyLines = HowManyLines({ token });

  const stats = await howManyLines.generateStats();

  const lineCount = howManyLines.calcLineCount(stats);

  return lineCount;
}

/// Trick to write to stdout when API fetch to finish with `async` syntax
(async () => process.stdout.write(`${await fetchLineCount()}`))();
