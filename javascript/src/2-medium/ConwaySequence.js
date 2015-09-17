var R = parseInt(readline());
var L = parseInt(readline());

debug(R);
debug(L);

var currentLine = [R].map(function (str) {
    return parseInt(str, 10)
});
newLine = currentLine;
for (var i = 1; i < L; i++) {
    debug(currentLine);
    var previous = undefined;
    var current;
    var count = 1;
    var newLine = [];
    currentLine.forEach(function (char) {
        current = char;
        if (previous == undefined) {
            previous = char;
            return;
        }
        //debug(previous + " " + current + " " + count);
        if (previous == current) {
            count++;
        } else {
            newLine.push(count)
            newLine.push(previous);
            count = 1;
            previous = char;
        }
    });
    if (count != undefined) {
        newLine.push(count)
    }
    newLine.push(current);
    currentLine = newLine;
}


// Write an action using print()
// To debug: printErr('Debug messages...');

print(newLine.join(' '));

function debug(value) {
    printErr(JSON.stringify(value));
}