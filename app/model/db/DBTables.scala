package model.db

import slick.lifted.TableQuery

object DBTables {
    // base query select * from
    val jobTable = TableQuery[JobTable]
}
