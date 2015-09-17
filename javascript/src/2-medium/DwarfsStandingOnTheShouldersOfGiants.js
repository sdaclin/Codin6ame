var n = readline();
var nodes = {};

for (var i = 0; i < n; i++) {
    var line = readline();
    var connection = line.split(' ');
    var from = parseInt(connection[0], 10);
    var to = parseInt(connection[1], 10);
    if (nodes[from] == undefined) {
        nodes[from] = new Node(from);
    }
    nodes[from].connect(to);
}
debug(nodes);
var maxConnection = 0;
for (var idx in nodes) {
    for (var idx1 in nodes) {
        nodes[idx1].alreadySeen = false;
    }
    var result = nodes[idx].computeMaxConnections();
    if (result > maxConnection) {
        maxConnection = result;
    }
}
print(maxConnection);

function Node(value) {
    this.value = value;
    this.connections = [];
    this.alreadySeen = false;

    this.connect = function (otherNode) {
        if (nodes[otherNode] == undefined) {
            nodes[otherNode] = new Node(otherNode);
        }
        this.connections.push(nodes[otherNode]);
    }

    this.computeMaxConnections = function () {
        this.alreadySeen = true;
        var maxConnections = 1;
        for (var idx in this.connections) {
            if (!this.connections[idx].alreadySeen) {
                result = 1 + this.connections[idx].computeMaxConnections();
                if (result > maxConnections) {
                    maxConnections = result;
                }
            }
        }
        return maxConnections;
    }
}

function debug(value) {
    printErr(JSON.stringify(value));
}