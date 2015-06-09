package thredds.server.catalog.tracker;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import thredds.client.catalog.Access;
import thredds.client.catalog.Dataset;
import thredds.server.catalog.DatasetScan;
import thredds.server.catalog.FeatureCollectionRef;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;

/**
 * Description
 *
 * @author John
 * @since 6/8/2015
 */
public class DatasetTrackerChronicle implements DatasetTracker {

  final ChronicleMap<String, Externalizable> map;
  private int count = 0;

  public DatasetTrackerChronicle() throws IOException {
    String pathname = "C:/temp/chronicleTest/cats.dat";
    File file = new File(pathname);

    ChronicleMapBuilder<String, Externalizable> builder = ChronicleMapBuilder.of(String.class, Externalizable.class)
             .averageValueSize(400).entries(1000 * 1100);

    map = builder.createPersistedTo(file);
  }

  @Override
  public boolean trackDataset(Dataset dataset, Callback callback) {
    if (callback != null) {
      callback.hasDataset(dataset);
      if (dataset.getRestrictAccess() != null) {
        callback.hasRestriction(dataset);
      }
      if (dataset.getNcmlElement() != null) {
        callback.hasNcml(dataset);
      }
    }

    boolean hasRestrict = dataset.getRestrictAccess() != null;
    boolean hasNcml = (dataset.getNcmlElement() == null) && !(dataset instanceof DatasetScan) && (dataset instanceof FeatureCollectionRef);
    if (!hasRestrict && !hasNcml) return false;

    String path = null;
    for (Access access : dataset.getAccess()) {

      String accessPath = access.getUrlPath();
      if (accessPath == null)
        System.out.println("HEY");
      if (path == null) path = accessPath;
      else if (!path.equals(access.getUrlPath())) {
        System.out.printf(" %s%n %s%n%n", path, accessPath);
      }
    }
    if (path == null)
      return false;

    CatalogExt dsext = new CatalogExt(dataset, hasNcml);
    map.put(path, dsext);
    return true;
  }

  @Override
  public String findResourceControl(String path) {
    CatalogExt dext = (CatalogExt) map.get(path);
    if (dext == null) return null;
    return dext.getRestrictAccess();
  }

  @Override
  public String findNcml(String path) {
    CatalogExt dext = (CatalogExt) map.get(path);
    if (dext == null) return null;
    return dext.getNcml();
  }

  public void close() {
    map.close();
  }
}
