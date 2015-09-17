/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
var houses = [];

var N = parseInt(readline());
for (var i = 0; i < N; i++) {
    var inputs = readline().split(' ');
    var X = parseInt(inputs[0]);
    var Y = parseInt(inputs[1]);
    houses.push(new House(i, X, Y));
}
//debug(houses);

var minMaxDistanceBackbone = houses.map(function (coord) {
    return coord.x
}).reduce(function (acc, val) {
    if (acc.min == null || acc.min > val) {
        acc.min = val;
    }
    if (acc.max == null || acc.max < val) {
        acc.max = val;
    }
    return acc;
}, {min: null, max: null});
var bakboneLength = minMaxDistanceBackbone.max - minMaxDistanceBackbone.min;
//debug(bakboneLength);

var housesOrdsAsc = houses.map(function (house) {
    return house.y
}).sort(function (a, b) {
    return a > b;
})
//debug(housesOrdsAsc);
var mediane;
if (housesOrdsAsc.length % 2 == 1) {
    mediane = housesOrdsAsc[(housesOrdsAsc.length - 1) / 2];
} else {
    mediane = (housesOrdsAsc[Math.floor((housesOrdsAsc.length - 1) / 2)] + housesOrdsAsc[Math.ceil((housesOrdsAsc.length - 1) / 2)]) / 2;
}
//debug(mediane);

var vLinkLengthFromAvgY = houses.map(function (coord) {
    return coord.y
}).reduce(function (a, b) {
    return a + Math.abs(b - mediane)
}, 0);
//debug(vLinkLengthFromAvgY);

print(bakboneLength + vLinkLengthFromAvgY);


function House(id, x, y) {
    this.id = id;
    this.x = x;
    this.y = y;
}

function debug(obj) {
    printErr(JSON.stringify(obj));
}