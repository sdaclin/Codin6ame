/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

var inputs = readline().split(' ');
var N = parseInt(inputs[0]); // the total number of nodes in the level, including the gateways
var L = parseInt(inputs[1]); // the number of links
var E = parseInt(inputs[2]); // the number of exit gateways

var nodes = {};
for (var i = 0; i < L; i++) {
    var inputs = readline().split(' ');
    var N1 = parseInt(inputs[0]); // N1 and N2 defines a link between these nodes
    var N2 = parseInt(inputs[1]);

    var nodeA = initOrGetNode(nodes, N1);
    var nodeB = initOrGetNode(nodes, N2);
    var edge = new Edge(nodeA, nodeB);
    nodeA.addEdge(edge);
    nodeB.addEdge(edge);
}
var gateaways = [];
for (var i = 0; i < E; i++) {
    var EI = parseInt(readline()); // the index of a gateway node
    var node = getNode(EI);
    node.setGateaway(true);
    gateaways.push(node);
}

// game loop
var roundId = -1;
//noinspection InfiniteLoopJS
while (true) {
    roundId++;
    var SI = parseInt(readline()); // The index of the node on which the Skynet agent is positioned this turn

    // If there is a gateaway on an adjacent node we close the corresponding edge
    edge = findAdjacentEdgeToGateaway(getNode(SI));
    if (edge == null) {

        //edge = getRandomEdgeLeadingToAGateaway();
        edge = getBestEdgeToCut(getNode(SI));
    }

    edge.close();
    print(edge.toString());

    // Write an action using print()
    // To debug: printErr('Debug messages...');
    //print('0 1'); // Example: 0 1 are the indices of the nodes you wish to sever the link between
}

//noinspection JSUnusedGlobalSymbols
function getRandomEdgeLeadingToAGateaway() {
    edge = gateaways[0].getEdges().pop();
    if (gateaways[0].getEdges().length == 0) {
        gateaways.shift();
    }
    return edge;
}

function computeGateawayWeightForNode(skynetAgentNode, lvl, roundId) {
    var gateawaysCount = 0;
    skynetAgentNode.forEachConnectedNode(function (node) {
        if (node.visited == roundId) {
            return;
        }
        node.visited = roundId;
        if (node.isGateaway) {
            gateawaysCount++;
        } else {
            gateawaysCount += computeGateawayWeightForNode(node, roundId);
        }
    });
    skynetAgentNode.gateawayCount = gateawaysCount;
}

function getBestEdgeToCut(skynetAgentNode, roundId) {
    // Need to find a path where there are < branch by edge than gateaway depth
    computeGateawayWeightForNode(skynetAgentNode, roundId);
    debug(skynetAgentNode);
}

function initOrGetNode(nodes, N1) {
    if (nodes[N1] == undefined) {
        nodes[N1] = new Node(N1);
    }
    return nodes[N1];
}

function getNode(N1) {
    return nodes[N1];
}

function Node(id) {
    //noinspection JSUnusedGlobalSymbols
    this.node = id;
    this.visited = -1;
    this.isGateaway = false;

    var edges = [];
    this.addEdge = function (edge) {
        edges.push(edge);
    };

    this.getId = function () {
        return id;
    };

    this.removeEdge = function (edge) {
        edges = edges.reduce(function (acc, currentEdge) {
            if (edge != currentEdge) {
                acc.push(currentEdge);
            }
            return acc;
        }, []);
    };

    this.setGateaway = function (value) {
        this.isGateaway = value;
    };

    this.forEachConnectedNode = function (callback) {
        var that = this;
        edges.forEach(function (edge) {
            callback(edge.passThrough(that));
        });
    };

    this.getEdges = function () {
        return edges;
    };
}

function Edge(n1, n2) {
    //noinspection JSUnusedGlobalSymbols
    this.edge = n1.getId() + " " + n2.getId();

    this.passThrough = function (nodeFrom) {
        if (nodeFrom == n1) {
            return n2;
        }
        if (nodeFrom == n2) {
            return n1;
        }
        throw "Node unknown " + nodeFrom;
    };

    this.toString = function () {
        return n1.getId() + ' ' + n2.getId();
    };

    this.close = function () {
        n1.removeEdge(this);
        n2.removeEdge(this);
    }
}

function findAdjacentEdgeToGateaway(agentNode) {
    var toReturnEdge = null;
    agentNode.getEdges().forEach(function (edge) {
        if (toReturnEdge != null) {
            return;
        }
        if (edge.passThrough(agentNode).isGateaway) {
            toReturnEdge = edge;
        }
    });
    return toReturnEdge;
}

function debug(value) {
    printErr(JSON.stringify(value));
}