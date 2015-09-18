var n = readline();

var horsesStr = [];
for (var i = 0; i < n; i++) {
    var str = readline();
    horsesStr.push(str);
}

var diff = null;
horsesStr.sort(function (a, b) {
    return a - b
}).reduce(function (a, b) {
    printErr(a + '|' + b);
    if (diff == null)
        diff = b - a;

    if (b - a < diff) {
        diff = b - a;
    }
    return b;
});

print(diff);