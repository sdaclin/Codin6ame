/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

var root = new Node();
debug(root);

var N = parseInt(readline());
for (var i = 0; i < N; i++) {
    var telephone = readline();
    root.processString(telephone);
}
var nodesNumber = root.countNode() - 1;
debug(root);
debug(nodesNumber);

// Write an action using print()
// To debug: printErr('Debug messages...');

print(nodesNumber); // The number of elements (referencing a number) stored in the structure.

function Node(value) {
    debug(value);
    this.childrens = {};
    if (value == undefined) {
        this.isRoot = true;
    } else {
        if (value.length != 1) {
            throw 'Something goes wrong';
        }
        this.value = value;
    }

    this.processString = function (content) {
        if (content.charAt(0).length == 0) {
            return;
        }
        if (this.childrens[content.charAt(0)] == undefined) {
            this.childrens[content.charAt(0)] = new Node(content.slice(0, 1));
        }
        this.childrens[content.charAt(0)].processString(content.slice(1));
    }

    this.countNode = function () {
        var countChildrenNode = 0;
        for (var key in this.childrens) {
            countChildrenNode += this.childrens[key].countNode();
        }
        return 1 + countChildrenNode;
    }
}

function debug(value) {
    printErr(JSON.stringify(value));
}
