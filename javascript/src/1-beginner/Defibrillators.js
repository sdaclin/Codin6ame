var LON = readline();
var LAT = readline();
var N = parseInt(readline());

var userCoord = {'lon': toRad(LON.replace(',', '.')), 'lat': toRad(LAT.replace(',', '.'))};

var defibs = [];
for (var i = 0; i < N; i++) {
    var DEFIB = readline();
    printErr(DEFIB);
    var arDefib = DEFIB.split(';');
    var defib = {
        'id': arDefib[0],
        'name': arDefib[1],
        'adress': arDefib[2],
        'phone': arDefib[3],
        'lon': toRad(arDefib[4].replace(',', '.')),
        'lat': toRad(arDefib[5].replace(',', '.'))
    };
    defibs.push(defib);
}

var nearest = defibs.reduce(function (a, b) {
    if (a == null)
        return b;

    printErr(a.name + ' ' + distance(userCoord, a));
    printErr(b.name + ' ' + distance(userCoord, b));
    if (distance(userCoord, a) < distance(userCoord, b)) {
        return a;
    } else {
        return b;
    }
});

print(nearest.name);

function distance(a, b) {
    displayPt(a);
    displayPt(b);

    var x = (b.lon - a.lon) * Math.cos((a.lat + b.lat) / 2);
    printErr('x ' + x);
    var y = (b.lat - a.lat);
    printErr('y ' + y);
    var distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) * 6371;
    printErr('distance ' + distance);
    return distance;
}

function toRad(angle) {
    var angleRad = angle * (Math.PI / 180);
    printErr('angleRad=' + angleRad);
    return angleRad;
}

function displayPt(a) {
    printErr('displayPt[' + a.lon + ',' + a.lat + ']');
}