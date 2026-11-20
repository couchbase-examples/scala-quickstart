package org.couchbase.scala.quickstart.controllers

import cats.implicits._
import com.couchbase.client.scala.Cluster
import com.couchbase.client.scala.query.{QueryOptions, QueryParameters, QueryScanConsistency}
import io.circe.Decoder
import org.couchbase.scala.quickstart.models.ListingInput

import scala.concurrent.{ExecutionContext, Future}

/** A parameterized SQL++ statement for a `/list` endpoint.
  *
  * The user-supplied search term, limit and skip are passed as named query
  * parameters, never interpolated into the statement text, so a search value
  * such as `x' OR '1'='1` is matched literally instead of changing the query.
  */
final case class ListingQuery(statement: String, parameters: Map[String, Any]) {
  def options: QueryOptions =
    QueryOptions()
      .parameters(QueryParameters.Named(parameters))
      .scanConsistency(QueryScanConsistency.RequestPlus())
}

object ListingQuery {
  val DefaultLimit = 5
  val DefaultSkip = 0

  /** Builds a case-insensitive "contains" search over `searchFields` of
    * `bucket`.`scope`.`collection`. Keyspace and field names come from
    * application code and config, not from the request.
    */
  def apply(
      bucket: String,
      scope: String,
      collection: String,
      searchFields: Seq[String],
      input: ListingInput
  ): ListingQuery = {
    require(searchFields.nonEmpty, "searchFields must not be empty")
    val where = searchFields.map(f => s"lower(a.$f) LIKE $$search").mkString(" OR ")
    val statement =
      s"SELECT a.* FROM `$bucket`.`$scope`.`$collection` a " +
        s"WHERE $where " +
        "LIMIT $limit OFFSET $skip"
    ListingQuery(
      statement,
      Map(
        "search" -> s"%${input.search.toLowerCase}%",
        "limit" -> input.limit.getOrElse(DefaultLimit),
        "skip" -> input.skip.getOrElse(DefaultSkip)
      )
    )
  }

  /** Runs the query and decodes every row as `T`; the first decode failure is returned as `Left`. */
  def run[T: Decoder](cluster: Cluster, query: ListingQuery)(implicit
      ec: ExecutionContext
  ): Future[Either[String, List[T]]] =
    for {
      result <- Future.fromTry(cluster.query(query.statement, query.options))
      rows <- Future.fromTry(result.rowsAs[io.circe.Json])
    } yield rows.toList.traverse(_.as[T].left.map(_.getMessage()))
}
