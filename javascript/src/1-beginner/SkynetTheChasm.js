var R = parseInt(readline()); // the length of the road before the gap.
var G = parseInt(readline()); // the length of the gap.
var L = parseInt(readline()); // the length of the landing platform.

// game loop
while (true) {
    var S = parseInt(readline()); // the motorbike's speed.
    var X = parseInt(readline()); // the position on the road of the motorbike.

    printErr('X =>' + X + '\n');
    printErr('R =>' + R + '\n');
    printErr('G =>' + G + '\n');


    if (S > G + 1) {
        print('SLOW');
        continue;
    }
    if (S < G + 1 && X < R) {
        print('SPEED');
        continue;
    }
    if (X + 1 == R) {
        print('JUMP');
        continue;
    }
    if (X > R) {
        print('SLOW');
        continue;
    }
    print('WAIT');
}