package model.db

import slick.lifted.TableQuery

object DBTables {
    val jobTable = TableQuery[JobTable]
    val salaryTable = TableQuery[SalaryTable]
}
