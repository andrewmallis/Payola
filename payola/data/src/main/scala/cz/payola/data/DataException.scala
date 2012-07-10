package cz.payola.data

object DataException
{
    def wrap[A](body: => A): A = {
        try {
            body
        } catch {
            case dataException: DataException => throw dataException
            case throwable: Throwable => {
                println("Error: " + throwable.toString)
                throw new DataException("An exception was thrown in the data layer.", Some(throwable))
            }
        }
    }
}

class DataException(val message: String, innerException: Option[Throwable] = None) extends Exception