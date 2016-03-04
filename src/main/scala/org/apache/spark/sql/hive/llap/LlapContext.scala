/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.hive.llap

import java.sql.Connection
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.catalyst.analysis.OverrideCatalog

class LlapContext(
  @transient override val sparkContext: SparkContext,
  val connectionUrl: String,
  @transient val connection: Connection, val userName: String)
    extends SQLContext(sparkContext) {
  @transient override lazy val catalog = getCatalog()

  def getCatalog() = {
    new HS2Catalog(this, connectionUrl, connection) with OverrideCatalog
  }

  def setCurrentDatabase(dbName: String) = {
    catalog.setCurrentDatabase(dbName)
  }

}

object LlapContext {
  def getUser(): String = {
    System.getProperty("hive_user", System.getProperty("user.name"))
  }

  def newInstance(sparkContext: SparkContext, connectionUrl: String) = {
    val userName: String = getUser()
    val conn = DefaultJDBCWrapper.getConnector(None, url = connectionUrl, userName)
    new LlapContext(sparkContext, connectionUrl, conn, userName)
  }
}

