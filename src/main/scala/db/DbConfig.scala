package io.daniel
package db

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

case class DbConfig(
    host: String,
    port: Int,
    user: String,
    database: String,
    password: String
) derives ConfigReader


