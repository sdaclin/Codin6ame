var MESSAGE = readline();

var chars = MESSAGE.split('');
printErr(chars);

var binaryRepresentation = chars.reduce(function (a, b) {
    return a + '' + padLeft(b.charCodeAt(0).toString(2));
}, '');

printErr(binaryRepresentation);

print(encode(binaryRepresentation));

function encode(binaryString) {
    var cntConsecutive = 0;
    var lastBit;
    var result = '';
    binaryString.split('').forEach(function (currentBit) {
        if (lastBit == null) {
            result += representation(currentBit) + ' 0';
        } else if (lastBit == currentBit) {
            result += '0';
        } else {
            result += ' ' + representation(currentBit) + ' 0';
        }
        lastBit = currentBit;
    });
    return result;
}

function representation(bit) {
    if (bit == '0') {
        return '00';
    } else {
        return '0';
    }
}

function padLeft(chaine) {
    while (chaine.length < 7) {
        chaine = '0' + chaine;
    }
    return chaine;
}