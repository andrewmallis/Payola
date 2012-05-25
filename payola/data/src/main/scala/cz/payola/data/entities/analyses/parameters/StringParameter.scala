package cz.payola.data.entities.analyses.parameters

import cz.payola.data.entities.analyses.Parameter
import cz.payola.data.PayolaDB

class StringParameter(
    override val id: String,
    name: String,
    defaultVal: String)
    extends cz.payola.domain.entities.analyses.parameters.StringParameter(name, defaultVal)
    with Parameter[String]
{
    private lazy val _values = PayolaDB.valuesOfStringParameters.left(this)

    // Get, store and set default value of parameter to Database
    val _defaultValueDb = defaultVal

    override def defaultValue = _defaultValueDb

    def parameterValues: Seq[StringParameterValue] = evaluateCollection(_values)
}


