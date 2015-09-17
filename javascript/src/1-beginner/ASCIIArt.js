var L = parseInt(readline());
var H = parseInt(readline());
var T = readline();

var Matrix = [];
for (var i = 0; i < H; i++) {
    Matrix.push(readline());
}

var letterToDisplay = T.split('');
printErr(letterToDisplay);

var outMatrix = [];
var step = 0;
letterToDisplay.forEach(function(currentLetter){
    var indexLettre = currentLetter.toUpperCase().charCodeAt(0) - 65;

    if (indexLettre<0 || indexLettre>25) {
        indexLettre=26;
    }

    // Pour la hauteur de la lettre
    for (var h = 0; h < H; h++){
        if (outMatrix[h] == null){
            outMatrix[h] = [];
        }
        // Pour la largeur de la lettre
        for (var l = 0; l < L; l++) {
            outMatrix[h][l + step * L] = Matrix[h][l+indexLettre * L];
        }
    }
    step++;
});
for (var h = 0; h < H; h++){
    print(outMatrix[h].join(''));
}