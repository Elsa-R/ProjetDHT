package helloWorld;

import peersim.edsim.*;
import peersim.core.*;
import peersim.config.*;


import java.util.ArrayList;
import java.util.Random;


/*
  Module d'initialisation de helloWorld: 
  Fonctionnement:
    pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative
    ensuite, il fait envoyer au noeud 0 un message "Hello" a tous les autres noeuds
 */
public class Initializer implements peersim.core.Control {
    
    private int helloWorldPid;
    private HelloWorld startNode;
    private ArrayList<HelloWorld> listeNode = new ArrayList<HelloWorld>(); 

    public Initializer(String prefix) {
		//recuperation du pid de la couche applicative
		this.helloWorldPid = Configuration.getPid(prefix + ".helloWorldProtocolPid");
		this.startNode = (HelloWorld)Network.get(0).getProtocol(this.helloWorldPid);
		startNode.setTransportLayer(0);
		this.startNode.addNeighbor(startNode); 
		startNode.turnOn();
    }

    public boolean execute() {
		int nodeNb;
	
		// recuperation de la taille du reseau
		nodeNb = Network.size();
	
		if (nodeNb < 1) {
			System.err.println("Network size is not positive");
			System.exit(1);
		}
	
		int totalNode = 5;
		//pour chaque noeud, on fait le lien entre la couche applicative et la couche transport
		for (int i = 1; i <= totalNode; i++) {
			int nodeId = this.getRandomNodeOff();
			HelloWorld node = (HelloWorld) Network.get(nodeId).getProtocol(this.helloWorldPid);
			node.setTransportLayer(nodeId);
			node.turnOn();
			this.listeNode.add(node);
			this.startNode.addNeighbor(node); 
		}
		
		this.affichageAnneau();
		
		//on fait partir un node
		int nodeIdOff = this.getRandomNodeOn();
		HelloWorld nodeOff = this.getNode(nodeIdOff);
		nodeOff.leave();
		this.listeNode.remove(nodeOff);
		System.out.println("\n Le node : "+nodeIdOff+" est parti \n");
		
		//stockage des donnes
		System.out.println("\n--- Stockage des données ---");
		ArrayList<Data> listeData = new ArrayList<Data>();
		listeData.add(new Data("livre"));
		listeData.add(new Data("Film"));
		listeData.add(new Data("Jeu"));
		for (Data data : listeData) {
			HelloWorld n = nodeForData(data);
			n.stockageData(data, 0, this.startNode);
		}
		this.affichageAnneau();
		
		System.out.println("Initialization completed");
		return false;
    }

	public int randomIntBetween(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;
	}

	//retourne l'id d'un node off
	public int getRandomNodeOff() {
		Random random = new Random();
		int nodeId = 0;
		while (this.getNode(nodeId).isTurnedOn() || nodeId == 0){
			nodeId = random.nextInt(Network.size());
		} 

		return nodeId;
	}
	
	//retourne l'id d'un node on
	public int getRandomNodeOn() {
		Random random = new Random();
		int nodeId = 0;
		while (this.getNode(nodeId).isTurnedOff() || nodeId == 0) {
			nodeId = random.nextInt(Network.size());
		} 
		return nodeId;
	}

	//retourne le node correspondant à l'id
	public HelloWorld getNode(int nodeId) {
		HelloWorld node = (HelloWorld) Network.get(nodeId).getProtocol(this.helloWorldPid);
		node.setTransportLayer(nodeId);
		return node;
	}
	
	//retourne le node ayant un uuid le plus proche d'une donnee
	public HelloWorld nodeForData(Data data) {
		HelloWorld nodeProche = null;
		Long uuid = data.getUUID();
		for (HelloWorld n: this.listeNode) {
			if (Math.abs(n.getUUID()-data.getUUID())<uuid) {
				nodeProche=n;
				uuid=n.getUUID();
			}
		}
		return nodeProche;
	}

	//affichage de l'anneau
	public void affichageAnneau() {
		System.out.println("\n\tVoici l'anneau :");
		HelloWorld currentNode = this.startNode;
		do {

			System.out.printf("%s \n\tuuid : %d\n\tVoisins :\n\t   gauche : %d \n\t   droit : %d \n", currentNode.toString(), currentNode.getUUID(), currentNode.getLeftNeighbor().getNodeId(), currentNode.getRightNeighbor().getNodeId());
			displayData(currentNode);
			int currentNodeId = currentNode.getRightNeighbor().getNodeId();
			currentNode = this.getNode(currentNodeId);

		} while (currentNode.getNodeId() != this.startNode.getNodeId());
	}

	public void displayData(HelloWorld node) {
		for (Data data : node.getlisteDonnees()) {
			System.out.println("\tData : ");
			System.out.println("\t uuid :" + data.getUUID() + " content : " + data.getContent());
		}
	}
}