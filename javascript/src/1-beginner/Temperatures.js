var N = parseInt(readline()); // the number of temperatures to analyse
var TEMPS = readline(); // the N temperatures expressed as integers ranging from -273 to 5526

printErr(N);
printErr(TEMPS);

if (TEMPS == null) {
    print(0);
}else{
    findClosestToZero(N, TEMPS.split(' '))
}

function findClosestToZero(n, temps) {
    var result = temps.reduce(function(a,b){
        printErr(Math.abs(a) + '|' + Math.abs(b));
        return Math.abs(a) < Math.abs(b) ? a : b;
    })
    print(result);
}