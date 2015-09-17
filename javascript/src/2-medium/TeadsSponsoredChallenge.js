var network = new Network();

var n = parseInt(readline()); // the number of adjacency relations
for (var i = 0; i < n; i++) {
    var inputs = readline().split(' ');
    var xi = parseInt(inputs[0]); // the ID of a person which is adjacent to yi
    var yi = parseInt(inputs[1]); // the ID of a person which is adjacent to xi

    network.addLink(new Link(xi, yi));
}

network.connect();

print(network.computeCenterNode(network.nodes[0]).maxDist);

function Network() {
    this.nodes = {};
    this.links = [];

    // For computation only
    this.nodesToProceed = [];

    this.addLink = function (link) {
        this.links.push(link);
    };

    this.resetNodeVisitedState = function () {
        for (var key in this.nodes) {
            this.nodes[key].visited = false;
        }
    };

    this.computeCenterNode = function (node, previousWeight) {
        var that = this;
        // Calcul du poid pour ce node
        var currentWeight = this.computeDepthForNode(node);

        // Si le poids n'est pas inférieur au precédent on retourne null
        if (previousWeight != null && !(currentWeight < previousWeight)) {
            return null;
        }

        var bestSibling;
        node.siblings.forEach(function (sibling) {
            var goodNode = bestSibling != null ? bestSibling : that.computeCenterNode(sibling, currentWeight);
            if (goodNode != null) {
                bestSibling = goodNode;
            }
        });
        if (bestSibling == null) {
            node.maxDist = currentWeight;
            return node;
        } else {
            return bestSibling;
        }
    };

    this.getLowestKey = function (objectWithIntegerKeys) {
        var keyToReturn = null;
        for (var key in objectWithIntegerKeys) {
            key = parseInt(key, 10);
            if (keyToReturn == null || key < keyToReturn) {
                keyToReturn = key;
            }
        }
        return keyToReturn;
    };

    this.computeDepthForNode = function (node) {
        var nodeToProceed = [];
        var nextLevelNodeToProceed;
        this.resetNodeVisitedState();
        nodeToProceed.push(node);
        var step = -1;

        while (nodeToProceed.length > 0) {
            step++;
            nextLevelNodeToProceed = [];
            nodeToProceed.forEach(function (node) {
                node.visited = true;
                node.siblings.forEach(function (sibling) {
                    if (!sibling.visited) {
                        nextLevelNodeToProceed.push(sibling);
                    }
                });
            });
            nodeToProceed = nextLevelNodeToProceed;
        }
        return step;
    };

    this.connect = function () {
        var that = this;
        this.links.forEach(function (link) {
            var nodeAIdx = link.nodes[0];
            var nodeBIdx = link.nodes[1];
            var nodeA;
            if (!(nodeA = that.nodes[nodeAIdx])) {
                nodeA = new Node(nodeAIdx);
                that.nodes[nodeAIdx] = nodeA;
            }
            if (!(nodeB = that.nodes[nodeBIdx])) {
                nodeB = new Node(nodeBIdx);
                that.nodes[nodeBIdx] = nodeB;
            }
            nodeA.addSibling(nodeB);
            nodeB.addSibling(nodeA);
        });
    };
}

function Node(id) {
    this.id = id;
    this.siblings = [];

    this.getId = function () {
        return this.id;
    };

    this.addSibling = function (siblingNode) {
        this.siblings.push(siblingNode);
    };
}

function Link(nodeIdxA, nodeIdxB) {
    this.nodes = [nodeIdxA, nodeIdxB];

    this.getNodesIdx = function () {
        return this.nodes;
    };

    this.toString = function () {
        return this.nodes[0] + ' ' + this.nodes[1];
    };

    this.concerns = function (node) {
        return node.getId() == nodeIdxA || node.getId() == nodeIdxB;
    };
}

function debug(value) {
    printErr(JSON.stringify(value));
}