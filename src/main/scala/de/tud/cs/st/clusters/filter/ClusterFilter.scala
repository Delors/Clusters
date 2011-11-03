package de.tud.cs.st.clusters.filter

trait ClusterFilter {
  type Graph
  type Dir

  def filter(clusters: Array[Graph], projectRootDir: Dir): Array[Graph]
}