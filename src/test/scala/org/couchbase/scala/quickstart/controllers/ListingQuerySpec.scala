package org.couchbase.scala.quickstart.controllers

import org.couchbase.scala.quickstart.models.ListingInput
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ListingQuerySpec extends AnyFlatSpec with Matchers {

  private def build(input: ListingInput) =
    ListingQuery("travel-sample", "inventory", "airline", Seq("name", "country"), input)

  "ListingQuery" should "target bucket.scope.collection and search every field" in {
    build(ListingInput(None, None, "x")).statement shouldBe
      "SELECT a.* FROM `travel-sample`.`inventory`.`airline` a " +
      "WHERE lower(a.name) LIKE $search OR lower(a.country) LIKE $search " +
      "LIMIT $limit OFFSET $skip"
  }

  it should "pass a lower-cased contains pattern and paging as named parameters" in {
    build(ListingInput(Some(10), Some(20), "United")).parameters shouldBe
      Map("search" -> "%united%", "limit" -> 10, "skip" -> 20)
  }

  it should "default limit to 5 and skip to 0" in {
    val params = build(ListingInput(None, None, "")).parameters
    params("limit") shouldBe 5
    params("skip") shouldBe 0
  }

  it should "never interpolate the search term into the statement" in {
    val hostile = "x' OR '1'='1"
    val query = build(ListingInput(None, None, hostile))
    query.statement should not include "'"
    query.statement should not include "1'='1"
    query.parameters("search") shouldBe "%x' or '1'='1%"
  }

  it should "reject an empty field list" in {
    an[IllegalArgumentException] should be thrownBy
      ListingQuery("b", "s", "c", Seq.empty, ListingInput(None, None, "x"))
  }
}
