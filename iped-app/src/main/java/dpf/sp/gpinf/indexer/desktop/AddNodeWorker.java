package dpf.sp.gpinf.indexer.desktop;

import java.util.Collection;
import java.util.HashSet;

import javax.swing.SwingWorker;

import org.kharon.Node;

import br.gov.pf.labld.graph.GraphService;
import br.gov.pf.labld.graph.GraphServiceFactoryImpl;
import br.gov.pf.labld.graph.NodeQueryListener;

class AddNodeWorker extends SwingWorker<Void, Node> implements NodeQueryListener {

  private final AppGraphAnalytics app;

  private Collection<Long> ids;
  private Collection<String> added = new HashSet<>();
  private Collection<Node> nodes = new HashSet<>();
  private int found = 0;

  public AddNodeWorker(AppGraphAnalytics appGraphAnalytics, Collection<Long> ids) {
    super();
    app = appGraphAnalytics;
    this.ids = ids;
  }

  @Override
  public boolean nodeFound(org.neo4j.graphdb.Node neo4jNode) {
    found++;
    app.increaseProgress((int) ((found / this.ids.size()) * 100));
    Node node = this.app.addNode(neo4jNode);
    if (node != null) {
      String nodeId = Long.toString(neo4jNode.getId());
      this.added.add(nodeId);
      this.nodes.add(node);

    }
    return true;
  }

  @Override
  protected Void doInBackground() throws Exception {
    app.setStatus(Messages.getString("GraphAnalysis.Processing"));
    app.setProgress(0);
    GraphService graphService = GraphServiceFactoryImpl.getInstance().getGraphService();
    graphService.getNodes(ids, this);
    app.addNodes(nodes);
    return null;
  }

  @Override
  protected void done() {
    app.setStatus(Messages.getString("GraphAnalysis.Done"));
    app.setProgress(100);
    app.graphPane.selectNodes(added);
  }

}