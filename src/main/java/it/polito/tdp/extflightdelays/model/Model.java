package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private  SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private Map<Integer, Airport> idMap;
	private ExtFlightDelaysDAO dao;
	private Map<Airport, Airport> visitaMap=new HashMap<Airport, Airport>();
	
	public Model() {
		idMap=new HashMap<Integer, Airport>();
		dao=new ExtFlightDelaysDAO();
		dao.loadAllAirports(idMap);
	}
		
	public void creaGrafo(int x) {
		this.grafo=new SimpleWeightedGraph<Airport, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//aggiungo vertici
		for(Airport a :idMap.values()) {
			if(dao.getAirlinesNumber(a)>x) {
				//inserisco aeroporto come vertice
				this.grafo.addVertex(a);
			}
			
			for(Rotta r: dao.getRotte(idMap)) {
				//controllo che non esista ancora l'arco, se esiste aggiorno peso
				if(this.grafo.containsVertex(r.getA1())&& this.grafo.containsVertex(r.getA2())) {
					DefaultWeightedEdge edge=this.grafo.getEdge(r.a1, r.a2);
					
					if(edge==null) {
						Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getPeso());
					}else {
						double peso=this.grafo.getEdgeWeight(edge);
						double pesoNuovo=peso+r.getPeso();
						this.grafo.setEdgeWeight(edge, pesoNuovo);
					}
				}
			}
		}
	}
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public Collection<Airport> getAeroporti(){
		return this.grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2) {
		List<Airport> percorso=new ArrayList<Airport>();
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> iterator=new BreadthFirstIterator<Airport, DefaultWeightedEdge>(this.grafo, a1);
		
		//aggiungo la radice del mio albero di visita
		visitaMap.put(a1, null);
		
		iterator.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {
			
			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport sorgente= grafo.getEdgeSource(e.getEdge());
				Airport destinazione=grafo.getEdgeTarget(e.getEdge());
				
				if(!visitaMap.containsKey(destinazione) && visitaMap.containsKey(sorgente)) {
					visitaMap.put(destinazione, sorgente);
				}else if(!visitaMap.containsKey(sorgente) && visitaMap.containsKey(destinazione)){
					visitaMap.put(sorgente, destinazione);
				}
			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		while(iterator.hasNext()) {
			iterator.next();
		}
		
		if(!visitaMap.containsKey(a1) || !visitaMap.containsKey(a2)) {
			//i due aeroporti non sono collegati
			return null;
		}
		
		Airport step= a2;
		
		while (step.equals(a1)) {
			percorso.add(step);
			step=visitaMap.get(step);
		}
		
		percorso.add(a1);
		
		return percorso;
		
	}
	
}
