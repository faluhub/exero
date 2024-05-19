const beautify = require("js-beautify");
const fs = require("fs");
const args = process.argv;

fs.readFile(args[3], "utf8", (e, data) => {
    if (e) { return; }
    const func = args[2] === "js" ? beautify.js : beautify.css;
    console.log(func(data));
});
