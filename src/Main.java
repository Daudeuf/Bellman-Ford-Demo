import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

// Une classe pour représenter un graphique connecté, dirigé et pondéré
class Graph {
	int V, E;
	Edge[] edge;

	// Crée un graphique avec des sommets V et des arêtes E
	Graph(int v, int e) {
		V = v;
		E = e;
		edge = new Edge[e];
		for (int i = 0; i < e; ++i)
			edge[i] = new Edge();
	}

	public static void main(String[] args) {
		int V = -1; // Nombre de sommets dans le graphique
		int E = -1; // Nombre d'arêtes dans le graphique

		Graph graph = null;

		try {
			Scanner scanner = new Scanner(getFile());
			int position = 0;

			while (scanner.hasNextLine()) {
				String[] ligne = scanner.nextLine().split("\\s");

				if (V == -1 || E == -1) {
					V = Integer.parseInt(ligne[0]);
					E = Integer.parseInt(ligne[1]);

					graph = new Graph(V, E);
				} else {
					graph.edge[position].src = Integer.parseInt(ligne[0]);
					graph.edge[position].dest = Integer.parseInt(ligne[1]);
					graph.edge[position++].weight = Integer.parseInt(ligne[2]);
				}
			}

			scanner.close();

		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		assert graph != null;
		String str = graph.BellmanFord(graph);

		JFrame frame = new JFrame("Graph Visualization");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new GraphView(graph));
		frame.pack();
		frame.setVisible(true);


		JPanel pnl = new JPanel();
		pnl.add(new JLabel(String.format("<html>%s</html>", str.replace("\n", "<br>"))));

		JFrame frame2 = new JFrame("Belman ford resultat");
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame2.setLocation(frame.getX() + frame.getWidth() + 20, frame2.getY());
		frame2.add(pnl);
		frame2.pack();
		frame2.setVisible(true);

		frame.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				frame2.setLocation(frame.getX() + frame.getWidth() + 20, frame2.getY());
			}

			@Override
			public void componentMoved(ComponentEvent e) {}

			@Override
			public void componentShown(ComponentEvent e) {}

			@Override
			public void componentHidden(ComponentEvent e) {}
		});
	}

	public static File getFile() {
		// Choisir le fichier avec une fenêtre de sélection de fichiers
		JFileChooser fileChooser = new JFileChooser();

		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else {
			return new File("graph.txt");
		}
	}

	// La fonction principale qui trouve les distances les plus courtes depuis la
	// src vers tous les autres sommets en utilisant Bellman-Ford
	// La fonction détecte également le poids négatif
	String BellmanFord(Graph graph) {
		int V = graph.V, E = graph.E;
		int[] dist = new int[V];

		// Étape 1 : Initialiser les distances de src à tous
		// les autres sommets comme INFINITE
		Arrays.fill(dist, Integer.MAX_VALUE);
		dist[0] = 0;

		// Étape 2 : Détendez tous les bords |V| - 1 fois.
		// Le chemin le plus court de src à n'importe quel autre sommet peut
		// avoir au plus |V| - 1 bords
		for (int i = 1; i < V; ++i) {
			for (int j = 0; j < E; ++j) {
				int u = graph.edge[j].src;
				int v = graph.edge[j].dest;
				int weight = graph.edge[j].weight;
				if (dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) dist[v] = dist[u] + weight;
			}
		}

		// Étape 3 : vérifier les cycles à poids négatif.
		// De l'étape ci-dessus garantit les distances les plus courtes si le graphique
		// ne contient pas de cycle de poids négatif. Si nous obtenons
		// un chemin plus court, alors il y a un cycle.
		for (int j = 0; j < E; ++j) {
			int u = graph.edge[j].src;
			int v = graph.edge[j].dest;
			int weight = graph.edge[j].weight;
			if (dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) {
				System.out.println("Le graphique contient un cycle de poids négatif");
				return null;
			}
		}

		return printArr(dist, V);
	}

	// Affichage de la solution
	private String printArr(int[] dist, int V) {
		StringBuilder str = new StringBuilder("Distance du sommet à la source :\n\tSommet\t\tCoût\n");

		for (int i = 0; i < V; ++i) {
			str.append(String.format("\t%s\t\t\t%s\n", (char) ('A' + i), dist[i] == Integer.MAX_VALUE ? "∞" : dist[i]));
		}

		return str.toString();
	}

	static class Edge {
		int src, dest, weight;

		Edge() {
			src = dest = weight = 0;
		}
	}

	public static class GraphView extends JPanel {

		private final Graph graph; // Reference to your Graph object

		GraphView(Graph graph) {
			this.graph = graph;
			setPreferredSize(new Dimension(400, 400)); // Set preferred size for the panel
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;

			// Define node radius and some padding
			int nodeRadius = 10;
			int padding = 20; // marge

			// Calculate spacing based on available space and number of nodes
			int width = getWidth() - 2 * padding;
			int height = getHeight() - 2 * padding;

			int d = Math.min(width, height);
			// int d = (int) (graph.V * 150 / Math.PI);

			// Draw edges as lines
			for (int i = 0; i < graph.E; i++) {
				int sourceX = padding + getX(d, graph.edge[i].src, graph.V);
				int sourceY = padding + getY(d, graph.edge[i].src, graph.V);
				int destX = padding + getX(d, graph.edge[i].dest, graph.V);
				int destY = padding + getY(d, graph.edge[i].dest, graph.V);

				g2d.drawLine(sourceX, sourceY, destX, destY);
			}

			// Draw nodes as circles
			for (int i = 0; i < graph.V; i++) {
				int x = padding + getX(d, i, graph.V);
				int y = padding + getY(d, i, graph.V);

				g2d.fill(new Ellipse2D.Double(x - nodeRadius, y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius));

				g2d.setColor(Color.WHITE);
				g2d.drawString("" + (char) ('A' + i), x - 5, y + 5);
				g2d.setColor(Color.BLACK);
			}
		}

		private int getX(int d, int i, int totI) {
			return (int) (d / 2 * Math.cos(360.0 / totI * i)) + d / 2;
		}

		private int getY(int d, int i, int totI) {
			return (int) (d / 2 * Math.sin(360.0 / totI * i)) + d / 2;
		}
	}

}
