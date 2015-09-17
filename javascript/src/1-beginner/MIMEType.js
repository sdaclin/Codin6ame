var N = parseInt(readline()); // Number of elements which make up the association table.
var Q = parseInt(readline()); // Number Q of file names to be analyzed.
var MimeTypes = {};
for (var i = 0; i < N; i++) {
    var inputs = readline().split(' ');
    var EXT = inputs[0]; // file extension
    var MT = inputs[1]; // MIME type.
    MimeTypes[EXT.toLowerCase()] = MT;
    //printErr(EXT + '=>' + MimeTypes[EXT]);
}
for (var i = 0; i < Q; i++) {
    var FNAME = readline(); // One file name per line.
    print(printMimeType(FNAME));
}

function printMimeType(fileName) {
    var fileParts = fileName.split('.');
    printErr(fileParts);
    if (fileParts.length == 1) {
        return 'UNKNOWN';
    }
    var extension = fileParts[fileParts.length - 1].toLowerCase();
    printErr(extension + '=>' + MimeTypes[extension]);
    if (MimeTypes[extension] == undefined) {
        return 'UNKNOWN';
    }
    return MimeTypes[extension];
}