var inputs = readline().split(' ');
var W = parseInt(inputs[0]); // width of the building.
var H = parseInt(inputs[1]); // height of the building.
var N = parseInt(readline()); // maximum number of turns before game over.
var inputs = readline().split(' ');
var X0 = parseInt(inputs[0]);
var Y0 = parseInt(inputs[1]);

var min = [];
var max = [];

var PLUS = X = 0;
var MOINS = Y = 1;

min[X] = min[Y] = 0;
max[Y] = H;
max[X] = W;

// game loop
while (true) {
    var BOMB_DIR = readline(); // the direction of the bombs from batman's current location (U, UR, R, DR, D, DL, L or UL)

    printErr(BOMB_DIR);
    if (BOMB_DIR.indexOf('D') != -1) {
        Y0 = newPos(PLUS, Y, Y0);
    } else if (BOMB_DIR.indexOf('U') != -1) {
        Y0 = newPos(MOINS, Y, Y0);
    }

    if (BOMB_DIR.indexOf('R') != -1) {
        X0 = newPos(PLUS, X, X0);
    } else if (BOMB_DIR.indexOf('L') != -1) {
        X0 = newPos(MOINS, X, X0);
    }

    // Write an action using print()
    // To debug: printErr('Debug messages...');

    print(X0 + ' ' + Y0); // the location of the next window Batman should jump to.
}

function newPos(dir, axe, current) {
    printErr('[' + dir + '] current=>' + current);
    if (dir == PLUS) {
        min[axe] = current;
        var maximum = max[axe];
        return Math.floor((maximum - current) / 2 + current);
    } else {
        max[axe] = current;
        var minimum = min[axe];
        return Math.floor((current - minimum) / 2 + minimum);
    }
}