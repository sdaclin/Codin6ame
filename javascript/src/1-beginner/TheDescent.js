// game loop
while (true) {
    var inputs = readline().split(' ');
    var SX = parseInt(inputs[0]);
    var SY = parseInt(inputs[1]);
    var mountains = [];
    for (var i = 0; i < 8; i++) {
        var MH = parseInt(readline()); // represents the height of one mountain, from 9 to 0. Mountain heights are provided from left to right.
        mountains.push({x:i,height:MH});
    }

    mountains.sort(function(a,b){
        return b.height - a.height;
    });

    var highestMountain = mountains.shift();
    printErr(SX);
    printErr(highestMountain.x);
    if(SX==highestMountain.x) {
        print('FIRE');
        continue;
    }

    print('HOLD');
}
