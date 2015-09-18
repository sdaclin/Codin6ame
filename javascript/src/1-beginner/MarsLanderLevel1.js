var N = parseInt(readline()); // the number of points used to draw the surface of Mars.
var ground = [];
for (var i = 0; i < N; i++) {
    var inputs = readline().split(' ');
    var LAND_X = parseInt(inputs[0]); // X coordinate of a surface point. (0 to 6999)
    var LAND_Y = parseInt(inputs[1]); // Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the surface of Mars.
    ground.push({x: LAND_X, y: LAND_Y});
}

var objectif;
ground.reduce(function (a, b) {
    if (a == null)
        return b;
    if (a.y == b.y) {
        //printPt(a);
        //printPt(b);
        objectif = {x: a.x + (b.x - a.x) / 2, y: a.y, l: (b.x - a.x)};
    }
    return b;
});
printPt(objectif);


// game loop
while (true) {
    var inputs = readline().split(' ');
    var X = parseInt(inputs[0]);
    var Y = parseInt(inputs[1]);
    var HS = parseInt(inputs[2]); // the horizontal speed (in m/s), can be negative.
    var VS = parseInt(inputs[3]); // the vertical speed (in m/s), can be negative.
    var F = parseInt(inputs[4]); // the quantity of remaining fuel in liters.
    var R = parseInt(inputs[5]); // the rotation angle in degrees (-90 to 90).
    var P = parseInt(inputs[6]); // the thrust power (0 to 4).

    var distanceToGround = Y - objectif.y;
    //printErr(distanceToGround);

    // Below some crappy but test passing code ;)
    if (VS < -39) {
        P = 4;
    }
    if (VS > -40) {
        P = 3;
    }
    if (VS > -35) {
        P = 2;
    }
    if (VS > -30) {
        P = 1;
    }
    if (VS > -25) {
        P = 0;
    }

    print('0 ' + P); // R P. R is the desired rotation angle. P is the desired thrust power.
}

function printPt(pt) {
    printErr('[' + pt.x + "|" + pt.y + ']');
}
