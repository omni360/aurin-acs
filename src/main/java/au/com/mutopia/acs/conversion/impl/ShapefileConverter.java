package au.com.mutopia.acs.conversion.impl;

import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.util.Ogr2Ogr;
import au.com.mutopia.acs.util.ZipUtils;
import com.google.common.io.Files;
import com.google.inject.Inject;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Log4j
public class ShapefileConverter implements Converter {

  private final KmlConverter kmlConverter;

  @Inject
  public ShapefileConverter(KmlConverter kmlConverter) {
    this.kmlConverter = kmlConverter;
  }

  public List<C3mlEntity> convert(Asset asset) throws ConversionException {
    try {
      // Extract all Shapefiles to be converted. A zip file may have multiple Shapefiles,
      // with each Shapefile containing c3mlEntities of a single geometry type.
      List<File> shapefiles = new ArrayList<>();
      String fileExtension = Files.getFileExtension(asset.getFileName());
      File assetTemporaryFile = asset.getTemporaryFile();
      if (fileExtension.equals(Format.ZIP.toString())) {
        List<File> unzippedFiles = ZipUtils.unzipToTempDirectory(assetTemporaryFile);
        for (File unzippedFile : unzippedFiles) {
          if (Files.getFileExtension(unzippedFile.getName()).equals(Format.SHP.toString())) {
            shapefiles.add(unzippedFile);
          }
        }
      } else {
        shapefiles.add(assetTemporaryFile);
      }
      if (shapefiles.isEmpty()) {
        throw new ConversionException("Failed to find .shp file.");
      }
      List<C3mlEntity> c3mlEntities = new ArrayList<>();
      for (File shapefile : shapefiles) {
        File kml = Ogr2Ogr.convertToKml(shapefile);
        c3mlEntities.addAll(kmlConverter.convert(new Asset(kml)));
      }
      c3mlEntities.forEach(e -> {
        e.getParameters().remove("Name");
      });
      return c3mlEntities;
    } catch (IOException e) {
      throw new ConversionException("Failed to read converted KML file", e);
    }
  }

  // /** The standard coordinate reference system to convert to. */
  // private static final CoordinateReferenceSystem STANDARD_CRS;
  // private static final Map<String, GeometryExtractor> EXTRACTOR_MAP = ImmutableMap.of("Point",
  // new PointExtract(), "LineString", new LineExtract(), "LinearRing", new LineExtract(),
  // "Polygon", new PolygonExtract());
  //
  // static {
  // try {
  // STANDARD_CRS = CRS.getAuthorityFactory(true).createCoordinateReferenceSystem("EPSG:4326");
  // } catch (Exception e) {
  // throw new IllegalStateException("Failed to create standard coordinate reference system");
  // }
  // }
  //
  // private File shapefile;
  //
  // public ShapefileConverter() {}
  //
  // /**
  // * Converts the zipped Shapefile {@link Asset} into a {@link C3mlEntity}.
  // *
  // * @param asset An {@link Asset} representing a zipped bundle of Shapefile files.
  // * @return A {@link C3mlEntity} containing the same information as the Shapefile.
  // */
  // public List<C3mlEntity> convert(Asset asset) throws ConversionException {
  // try {
  // File shpFile = extractShapefile(asset);
  // return getEntities(shpFile);
  // } catch (IOException e) {
  // throw new ConversionException("Failed to convert Shapefile", e);
  // }
  // }
  //
  // /**
  // * Extracts the .shp file from a zipped Shapefile archive.
  // *
  // * @throws IOException
  // */
  // private File extractShapefile(Asset asset) throws IOException {
  // File file = FileUtils.createTemporaryFileWithContent(asset.getData());
  // List<File> unzippedFiles = ZipUtils.unzipToTempDirectory(file);
  // for (File unzippedFile : unzippedFiles) {
  // if (Files.getFileExtension(unzippedFile.getName()).equals("shp")) {
  // return unzippedFile;
  // }
  // }
  // throw new IllegalArgumentException("No SHP file found in archive");
  // }
  //
  // /**
  // * Extracts and populates a {@link C3mlEntity} for each feature in the given Shapefile.
  // *
  // * @param shpFile The Shapefile to extract from.
  // * @return A list of the extracted {@link C3mlEntity} objects.S
  // * @throws IOException
  // */
  // public List<C3mlEntity> getEntities(File shpFile) throws IOException {
  // SimpleFeatureSource source = getFeatureSource(shpFile);
  // List<String> columnHeaders = getColumnHeaders(source);
  // columnHeaders.remove("the_geom");
  //
  // return getFeatures(source).stream().map(feature -> getEntity(feature, columnHeaders))
  // .collect(Collectors.toList());
  // }
  //
  // /**
  // * Creates a {@link C3mlEntity} from a Shapefile feature and list of column names.
  // *
  // * @param feature The Shapefile feature to be converted into a {@link C3mlEntity}.
  // * @param columnHeaders The list of column headers for the Shapefile feature.
  // * @return The constructed {@link C3mlEntity}.
  // */
  // private C3mlEntity getEntity(SimpleFeature feature, List<String> columnHeaders) {
  // C3mlEntity entity = new C3mlEntity(UUID.randomUUID());
  // entity.setName(feature.getID());
  // entity.setParameters(readFeatureParameters(feature, columnHeaders));
  // writeGeometricValues(feature, entity);
  //
  // return entity;
  // }
  //
  // private void writeGeometricValues(SimpleFeature feature, C3mlEntity entity) {
  // Object defaultGeometry = feature.getDefaultGeometry();
  // if (defaultGeometry == null) {
  // return;
  // }
  //
  // if (defaultGeometry instanceof GeometryCollection) {
  // List<C3mlEntity> geoObjects =
  // unwrapMultiGeometry(entity.getName(), (GeometryCollection) defaultGeometry);
  // if (geoObjects.size() == 1) {
  // entity.setGeometry(geoObjects.get(0).getGeometry());
  // } else {
  // for (C3mlEntity childGeoObject : geoObjects) {
  // entity.addChild(childGeoObject);
  // }
  // }
  // } else {
  // GeoLeaf geoLeaf = getGeoLeaf((Geometry) defaultGeometry);
  // if (geoLeaf != null) {
  // entity.setGeometry(geoLeaf);
  // }
  // }
  // }
  //
  // /**
  // * Extracts a map of parameter names and values from the given feature.
  // *
  // * @param feature The feature to extract parameter values from.
  // * @param columnHeaders The headers of the Shapefile columns, used as the names of the
  // parameters.
  // * @return A map of parameter names to values.
  // */
  // private Map<String, String> readFeatureParameters(SimpleFeature feature,
  // List<String> columnHeaders) {
  // Map<String, String> parameters = new HashMap<>();
  // for (String header : columnHeaders) {
  // Object attribute = feature.getAttribute(header);
  // if (attribute != null) {
  // parameters.put(header, attribute.toString());
  // }
  // }
  // return parameters;
  // }
  //
  // private GeoLeaf getGeoLeaf(Geometry geometry) {
  // return EXTRACTOR_MAP.get(geometry.getGeometryType()).convert(geometry);
  // }
  //
  // /**
  // * Unwraps shapefile's multi geometry, creates a new geo object for each geometry to be appended
  // * to parent geo object.
  // *
  // * @param parentName The name of the parent geo object, the child geo object will have similar
  // * name.
  // * @param geometryCollection The geometry collection to be unwrapped.
  // * @return The list of geo objects representing each geometry in the geometry collection.
  // */
  // private List<GeoObject> unwrapMultiGeometry(String parentName,
  // GeometryCollection geometryCollection) {
  // geometryCollection.getGeometryType();
  // int numOfElements = geometryCollection.getNumGeometries();
  // List<GeoObject> geoObjects = Lists.newArrayList();
  //
  // for (int i = 0; i < numOfElements; i++) {
  // Geometry geometry = geometryCollection.getGeometryN(i);
  // GeoObject newGeoObject = new GeoObject();
  // newGeoObject.setId(generateUniqueId());
  // newGeoObject.setName(parentName + "_" + i);
  // GeoLeaf geoLeaf = getGeoLeaf(geometry);
  // newGeoObject.setGeometry(geoLeaf);
  // geoObjects.add(newGeoObject);
  // }
  // return geoObjects;
  // }
  //
  //
  // /**
  // * Gets the feature source from the Shapefile.
  // */
  // private SimpleFeatureSource getFeatureSource(File shpFile) throws IOException {
  // FileDataStore store = null;
  // try {
  // store = FileDataStoreFinder.getDataStore(shpFile);
  // return store.getFeatureSource();
  // } catch (IOException e) {
  // throw new ConversionException("Couldn't find feature source for Shapefile", e);
  // } finally {
  // if (store != null) {
  // store.dispose();
  // }
  // }
  // }
  //
  // /**
  // * Gets the column headers. However, FeatureID is not included in the list of headers.
  // *
  // * @return a list of column headers found in the shapefile
  // */
  // private List<String> getColumnHeaders(SimpleFeatureSource source) throws IOException {
  // List<AttributeDescriptor> attrs = source.getFeatures().getSchema().getAttributeDescriptors();
  // return attrs.stream().map(att -> att.getLocalName()).collect(Collectors.toList());
  // }
  //
  // /**
  // * Gets a list of all features from the Shapefile's feature source.
  // *
  // * @return A list of all features.
  // * @throws ConversionException
  // */
  // private List<SimpleFeature> getFeatures(SimpleFeatureSource source) throws ConversionException
  // {
  // CoordinateReferenceSystem sourceCrs = getCrs(source);
  // List<SimpleFeature> featureList = Lists.newArrayList();
  //
  // // Get an iterator over the features.
  // SimpleFeatureIterator iterator;
  // try {
  // iterator = source.getFeatures().features();
  // } catch (IOException e) {
  // throw new ConversionException("Failed to get feature iterator", e);
  // }
  //
  // try {
  // while (iterator.hasNext()) {
  // SimpleFeature feature = iterator.next();
  // if (sourceCrs != null) {
  // transformFeature(feature, sourceCrs);
  // }
  // featureList.add(feature);
  // }
  // } finally {
  // iterator.close();
  // }
  // return featureList;
  // }
  //
  // /**
  // * Converts and transforms the coordinates of the given feature.
  // *
  // * @param feature The feature to transform.
  // * @param crs The coordinate reference system (CRS) to transform from.
  // * @throws ConversionException
  // */
  // private void transformFeature(SimpleFeature feature, CoordinateReferenceSystem sourceCrs)
  // throws ConversionException {
  // try {
  // MathTransform transform = CRS.findMathTransform(sourceCrs, STANDARD_CRS, true);
  // Geometry sourceGeom = (Geometry) feature.getDefaultGeometry();
  // feature.setDefaultGeometry(JTS.transform(sourceGeom, transform));
  // } catch (Exception e) {
  // throw new ConversionException("Failed to transform feature CRS", e);
  // }
  // }
  //
  // /**
  // * Gets the coordinate reference system of the Shapefile feature source.
  // *
  // * @throws ConversionException
  // */
  // private CoordinateReferenceSystem getCrs(SimpleFeatureSource source) throws ConversionException
  // {
  // try {
  // return source.getSchema().getCoordinateReferenceSystem();
  // } catch (Exception e) {
  // throw new ConversionException("Error getting CRS from feature source", e);
  // }
  // }
  //
  // private interface GeometryExtractor {
  // GeoLeaf convert(Geometry geometry);
  // }
  //
  // private static class PointExtract implements GeometryExtractor {
  // @Override
  // public GeoLeaf convert(Geometry geometry) {
  // Point point = (Point) geometry;
  // GeoPoint geoPoint = new GeoPoint();
  // geoPoint.setPoint(new Vertex3D(point.getX(), point.getY(), 0.0, ShapeFile));
  // geoPoint.setColor(DEFAULT_COLOR);
  // return geoPoint;
  // }
  // }
  //
  // private static class LineExtract implements GeometryExtractor {
  // @Override
  // public GeoLeaf convert(Geometry geometry) {
  // LineString lineString = (LineString) geometry;
  // boolean isClosed = lineString.isClosed();
  // GeoLeaf geoLeaf = isClosed ? new GeoPolygon() : new GeoPolyLine();
  //
  // Coordinate[] coordinates = lineString.getCoordinates();
  //
  // for (Coordinate c : coordinates) {
  // if (isClosed) {
  // ((GeoPolygon) geoLeaf).addPoint(new Vertex3D(c.x, c.y, 0.0, ShapeFile));
  // ((GeoPolygon) geoLeaf).setColor(DEFAULT_COLOR);
  // } else {
  // ((GeoPolyLine) geoLeaf).addPoint(new Vertex3D(c.x, c.y, 0.0, ShapeFile));
  // ((GeoPolyLine) geoLeaf).setColor(DEFAULT_COLOR);
  // }
  // }
  //
  // return geoLeaf;
  // }
  // }
  //
  // private static class PolygonExtract implements GeometryExtractor {
  // @Override
  // public GeoLeaf convert(Geometry geometry) {
  // Polygon polygon = (Polygon) geometry;
  // GeoPolygon geoPolygon = new GeoPolygon();
  // Geometry boundary = polygon.getBoundary();
  // Coordinate[] coordinates = boundary.getCoordinates();
  //
  // for (Coordinate c : coordinates) {
  // geoPolygon.addPoint(new Vertex3D(c.x, c.y, 0.0, ShapeFile));
  // }
  //
  // geoPolygon.setColor(DEFAULT_COLOR);
  // return geoPolygon;
  // }
  // }

}
