/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.docgen.tooling

import org.neo4j.cypher.GraphIcing
import org.scalatest.Assertions
import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import org.scalatest.Suite

class PrettifierTest extends Suite
                     with FunSuiteLike
                     with Assertions
                     with Matchers
                     with GraphIcing {

  test("should uppercase keywords") {
    actual("create        n") should equal(expected("CREATE n"))
  }

  test("may break CREATE INDEX FOR") {
    actual("create index for (p:Person) on (p.name)") should equal(expected("CREATE INDEX FOR (p:Person)%nON (p.name)"))
  }

  test("may break CREATE INDEX IF NOT EXISTS FOR") {
    actual("create index if not exists for (p:Person) on (p.name)") should equal(expected("CREATE INDEX IF NOT EXISTS FOR (p:Person)%nON (p.name)"))
  }

  test("may break CREATE INDEX FOR with name") {
    actual("create index name for (p:Person) on (p.name)") should equal(expected("CREATE INDEX name FOR (p:Person)%nON (p.name)"))
  }

  test("may break CREATE INDEX IF NOT EXISTS FOR with name") {
    actual("create index name if not exists for (p:Person) on (p.name)") should equal(expected("CREATE INDEX name IF NOT EXISTS FOR (p:Person)%nON (p.name)"))
  }

  test("should not break DROP INDEX by name") {
    actual("drop index name") should equal(expected("DROP INDEX name"))
  }

  test("should not break DROP INDEX IF EXISTS by name") {
    actual("drop index name if exists") should equal(expected("DROP INDEX name IF EXISTS"))
  }

  test("should not break on ASC") {
    actual("order by n.name asc") should equal(expected("ORDER BY n.name ASC"))
  }

  test("should not break CREATE in FOREACH") {
    actual("match p=n foreach(x in p | create x--())") should equal(expected("MATCH p=n%nFOREACH (x IN p | CREATE x--())"))
  }

  test("should not break CREATE in complex FOREACH") {
    actual("match p=n foreach(x in p | create x--() set x.foo = 'bar') return distinct p;") should equal(
      expected("MATCH p=n%nFOREACH (x IN p | CREATE x--()%nSET x.foo = 'bar')%nRETURN DISTINCT p;")
    )
  }

  test("should not break STARTS WITH ") {
    actual("return 'apartment' starts with 'apa' as x") should equal(
      expected("RETURN 'apartment' STARTS WITH 'apa' AS x")
    )
  }

  test("should not break ENDS WITH ") {
    actual("return 'apartment' ends with 'apa' as x") should equal(
      expected("RETURN 'apartment' ENDS WITH 'apa' AS x")
    )
  }

  test("should not break CONSTRAINT ON") {
    actual("create constraint on (person:Person) assert person.age is unique") should equal(
      expected("CREATE CONSTRAINT ON (person:Person) ASSERT person.age IS UNIQUE")
    )
  }

  test("may break CREATE CONSTRAINT with name") {
    actual("create constraint name on (person:Person) assert person.age is unique") should equal(
      expected("CREATE CONSTRAINT name%nON (person:Person) ASSERT person.age IS UNIQUE")
    )
  }

  test("should break ON CREATE") {
    actual("merge n on create set n.age=32") should equal(expected("MERGE n%nON CREATE SET n.age=32"))
  }

  test("should correctly handle parenthesis in MATCH") {
    actual("match (a)-->(b) return b") should equal(expected("MATCH (a)-->(b)%nRETURN b"))
  }

  test("should uppercase multiple keywords") {
    actual("match (n) where n.name='B' return n") should equal(expected("MATCH (n)%nWHERE n.name='B'%nRETURN n"))
  }

  test("should uppercase multiple keywords 2") {
    actual("match (a) where a.name='A' return a.age as SomethingTotallyDifferent") should equal(
      expected("MATCH (a)%nWHERE a.name='A'%nRETURN a.age AS SomethingTotallyDifferent")
    )
  }

  test("should not break WHERE in comprehensions") {
    actual("return [x in range(0,10) where x + 2 = 0 | x^3] as result") should equal(
      expected("RETURN [x IN range(0,10) WHERE x + 2 = 0 | x^3] AS result")
    )
  }

  test("should uppercase extra keywords") {
    actual("match david--otherPerson-->() where david.name='David' with otherPerson, count(*) as foaf where foaf > 1 return otherPerson") should equal(
      expected("MATCH david--otherPerson-->()%nWHERE david.name='David'%nWITH otherPerson, count(*) AS foaf%nWHERE foaf > 1%nRETURN otherPerson")
    )

  }

  test("should not break after OPTIONAL") {
    actual("optional MATCH (n)-->(x) return n, x") should equal(expected("OPTIONAL MATCH (n)-->(x)%nRETURN n, x"))
  }

  test("should handle LOAD CSV") {
    actual("LOAD CSV FROM \"f\" AS line") should equal(expected("LOAD CSV FROM \"f\" AS line"))
  }

  test("should handle LOAD CSV WITH HEADERS") {
    actual("LOAD CSV wiTh HEADERS FROM \"f\" AS line") should equal(expected("LOAD CSV WITH HEADERS FROM \"f\" AS line"))

  }

  test("should prettify and break LOAD CSV") {
    actual("MATCH (n) LOAD CSV FROM \"f\" AS line return (n)") should equal(
      expected("MATCH (n)%nLOAD CSV FROM \"f\" AS line%nRETURN (n)")
    )
  }

  test("should not break after DETACH in DETACH DELETE") {
    actual("MATCH (n) DETACH DELETE (n)") should equal(
      expected("MATCH (n)%nDETACH DELETE (n)")
    )
  }

  test("should prettify and break USING PERIODIC COMMIT LOAD CSV") {
    actual("using periodic commit match () MATCH (n) LOAD CSV FROM \"f\" AS line return (n)") should equal(
      expected("USING PERIODIC COMMIT%nMATCH ()%nMATCH (n)%nLOAD CSV FROM \"f\" AS line%nRETURN (n)")
    )
  }

  test("should prettify with correct string quotes") {
    actual("mATCH a WhERE a.name='A' RETURN a.age > 30, \"I'm a literal\", a-->()") should equal(
      expected("MATCH a%nWHERE a.name='A'%nRETURN a.age > 30, \"I'm a literal\", a-->()")
    )
  }

  test("should handle join hints") {
    actual("match (a:A)-->(b:B) USING join ON b return a.prop") should equal(
      expected("MATCH (a:A)-->(b:B)%nUSING JOIN ON b%nRETURN a.prop")
    )
  }

  test("should handle CALL YIELD") {
    actual("match (n) call db.indexes yield state RETURN *") should equal(expected("MATCH (n)%nCALL db.indexes YIELD state%nRETURN *"))
  }

  test("MERGE should start on a new line") {
    actual("MERGE (a:A) MERGE (b:B) MERGE (a)-[:T]->(b) RETURN *") should equal(expected(
      "MERGE (a:A)%nMERGE (b:B)%nMERGE (a)-[:T]->(b)%nRETURN *"))
  }

  test("UNWIND should start on a new line") {
    actual("WITH [1,2,2] AS coll UNWIND coll AS x RETURN collect(x)") should equal(expected("WITH [1,2,2] AS coll%nUNWIND coll AS x%nRETURN collect(x)"))
  }

  test("keep new lines, but apply all the other rules. Don't duplicate newline before RETURN") {
    actualKeepNL(
      """unwind [
        |        duration({ days: 14, hours:16, minutes: 12 }),
        |     duration({ months:   5, days: 1.5 }),
        |] as aDuration
        |return aDuration""".stripMargin
    ) should equal(expected(
      """UNWIND [
        |duration({ days: 14, hours:16, minutes: 12 }),
        |duration({ months: 5, days: 1.5 }),
        |] AS aDuration
        |RETURN aDuration""".stripMargin
    ))
  }

  test("enforce newlines before breaking keywords, even if keepNewlines is set") {
    actualKeepNL(
      """with
        |1
        |as one return one""".stripMargin
    ) should equal(expected(
      """WITH
        |1
        |AS one
        |RETURN one""".stripMargin
    ))
  }

  private def actual(text: String) = Prettifier(text)

  private def actualKeepNL(text: String) = Prettifier(text, keepMyNewlines = true)

  private def expected(text: String) = String.format(text)
}
