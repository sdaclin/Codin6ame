var inputs = readline().split(' ');
var LX = parseInt(inputs[0]); // the X position of the light of power
var LY = parseInt(inputs[1]); // the Y position of the light of power
var TX = parseInt(inputs[2]); // Thor's starting X position
var TY = parseInt(inputs[3]); // Thor's starting Y position

// game loop
while (true) {
    var E = parseInt(readline()); // The level of Thor's remaining energy, representing the number of moves he can still make.

    var hori;
    if (LX > TX) {
        hori = 'E';
        TX += 1;
    } else if (LX < TX) {
        hori = 'W';
        TX -= 1;
    } else {
        hori = '';
    }

    var vert;
    printErr('LY=>' + LY);
    printErr('TY=>' + TY);
    printErr('LY>TY=>' + (LY > TY));
    if (LY > TY) {
        vert = 'S';
        TY += 1;
    } else if (LY < TY) {
        vert = 'N';
        TY -= 1;
    } else {
        vert = '';
    }

    print(vert + hori); // A single line providing the move to be made: N NE E SE S SW W or NW
}