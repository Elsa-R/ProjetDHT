package helloWorld;
import peersim.edsim.*;
import peersim.core.*;
import peersim.config.*;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.UUID;

public class HelloWorld implements EDProtocol {
    
    //identifiant de la couche transport
    private int transportPid;

    //objet couche transport
    private HWTransport transport;

    //identifiant de la couche courante (la couche applicative)
    private int mypid;

    //le numero de noeud
    private int nodeId;
    
    private boolean on = false;
    
    private HelloWorld leftNeighborNode;

    private HelloWorld rightNeighborNode;

    private Long uuid = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    
    //prefixe de la couche (nom de la variable de protocole du fichier de config)
    private String prefix;
    
    private ArrayList<Data> listeDonnees = new ArrayList<>();

    public HelloWorld(String prefix) {
		this.prefix = prefix;
		//initialisation des identifiants a partir du fichier de configuration
		this.transportPid = Configuration.getPid(prefix + ".transport");
		this.mypid = Configuration.getPid(prefix + ".myself");
		this.transport = null;
    }

    //methode appelee lorsqu'un message est recu par le protocole HelloWorld du noeud
    public void processEvent( Node node, int pid, Object event ) {
    	this.receive((Message)event);
    }
    
    //methode necessaire pour la creation du reseau (qui se fait par clonage d'un prototype)
    public Object clone() {

		HelloWorld dolly = new HelloWorld(this.prefix);
	
		return dolly;
    }

    //liaison entre un objet de la couche applicative et un 
    //objet de la couche transport situes sur le meme noeud
    public void setTransportLayer(int nodeId) {
		this.nodeId = nodeId;
		this.transport = (HWTransport) Network.get(this.nodeId).getProtocol(this.transportPid);
    }

    //envoi d'un message
    public void send(Message msg) {
    	Node dest = Network.get(this.getClosestNodeForUUID(msg.getDest()).getNodeId());
    	this.transport.send(getMyNode(), dest, msg, this.mypid);
    }
    
    //affichage a la reception
    private void receive(Message msg) {
        if ( msg.getDest() == this.uuid) {
            System.out.println(this.nodeId + ": Received " + msg.getContent());
        } else {
            send(msg);
        }
    }
    
    //methodes get
    
    //retourne le noeud courant
    private Node getMyNode() {
    	return Network.get(this.nodeId);
    }
    

    public String toString() {
    	return "Node "+ this.nodeId;
    }
    

    public void turnOn() {
        this.on = true;
    }
    

    public void turnOff() {
        this.on = false;
    }

    public HelloWorld getRightNeighbor() {
        if (this.rightNeighborNode == null) {
            return this;
        }
        return this.rightNeighborNode;
    }

    public HelloWorld getLeftNeighbor() {
        if (this.leftNeighborNode == null) {
            return this;
        }
        return this.leftNeighborNode;
    }

    public boolean isTurnedOff() {
        return !this.on;
    }

    public boolean isTurnedOn() {
        return this.on;
    }

    public long getUUID() {
        return this.uuid;
    }

    public int getNodeId() {
        return this.nodeId;
    }

    public ArrayList<Data> getlisteDonnees() {
    	return this.listeDonnees;
    }
    
    //methodes set
    public void setLeftNeighborNode(HelloWorld node) {
        this.leftNeighborNode = node;
    }

    public void setRightNeighborNode(HelloWorld node) {
        this.rightNeighborNode = node;
    }
    
    public void setNeighbors(HelloWorld leftNeighborNode, HelloWorld rightNeighborNode) {
    	rightNeighborNode.setLeftNeighborNode(leftNeighborNode);
    	leftNeighborNode.setRightNeighborNode(rightNeighborNode);
    }
    
    //ajout des voisins
    public boolean addNeighbor(HelloWorld node) {
    	if (this.nodeId == 0 && this.rightNeighborNode==null){
    		setLeftNeighborNode(this);
    		setRightNeighborNode(this);
    	}
    	
        if (atRight(node)) {
            HelloWorld rightNeighborNode = this.getRightNeighbor();
            this.setNeighbors(this, node);
        	this.setNeighbors(node, rightNeighborNode);
            return true;

        } else if (atLeft(node)) {
            HelloWorld leftNeighborNode = this.getLeftNeighbor();
            this.setNeighbors(node, this);
        	this.setNeighbors(leftNeighborNode, node);
            return true;

        } else{
            return this.getRightNeighbor().addNeighbor(node);
        }
    }

    public boolean atRight(HelloWorld node) {
    	return node.nodeId < this.rightNeighborNode.nodeId || this.nodeId == this.rightNeighborNode.nodeId;
    }

    public boolean atLeft(HelloWorld node) {
    	return (this.nodeId == 0 && this.leftNeighborNode.nodeId < node.nodeId) ||(node.nodeId > this.leftNeighborNode.nodeId && node.nodeId < this.rightNeighborNode.nodeId);
    }


    public void leave() {
        this.turnOff();
        this.leftNeighborNode.setRightNeighborNode(this.rightNeighborNode);
        this.rightNeighborNode.setLeftNeighborNode(this.leftNeighborNode);
    }


    public void stockageData(Data data, int cpt, HelloWorld lastNode) {
    	this.listeDonnees.add(data);
    	this.getLeftNeighbor().listeDonnees.add(data);
    	this.getRightNeighbor().listeDonnees.add(data);
    }

    public HelloWorld getClosestNodeForUUID(long uuid) {
        TreeMap<Long, HelloWorld> dist = new TreeMap<Long, HelloWorld>();
        dist.put(Math.abs(this.getUUID() - uuid), this);
        dist.put(Math.abs(this.getLeftNeighbor().getUUID() - uuid), this.getLeftNeighbor());
        dist.put(Math.abs(this.getRightNeighbor().getUUID() - uuid), this.getRightNeighbor());
        return dist.firstEntry().getValue();
    }
}