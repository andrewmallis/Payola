package cz.payola.common.entities.settings

import scala.collection.immutable
import cz.payola.common.Entity

/**
  * Customization of appearance of a vertex with particular type.
  */
trait ClassCustomization extends Entity
{
    /** Type of the property customizations in the class customization. */
    type PropertyCustomizationType <: PropertyCustomization

    /** URI of the class. */
    val uri: String

    protected var _fillColor: String

    protected var _radius: Int

    protected var _glyph: String

    protected var _propertyCustomizations: immutable.Seq[PropertyCustomizationType]

    override def classNameText = "ontology class customization"

    /** Fill color of the vertex. */
    def fillColor = _fillColor

    /**
      * Sets fill color of the vertex.
      * @param value New value of the fill color.
      */
    def fillColor_=(value: String) {
        _fillColor = value
    }

    /** Radius of the vertex. */
    def radius = _radius

    /**
      * Sets radius of the vertex.
      * @param value New value of the radius.
      */
    def radius_=(value: Int) {
        _radius = value
    }

    /** Vertex glyph. */
    def glyph = _glyph

    /**
      * Sets the vertex glyph.
      * @param value New value of the vertex glyph.
      */
    def glyph_=(value: String) {
        validate(value.length <= 1, "glyph", "Glyph must be string with maximal lenght 1")
        _glyph = value
    }

    /** Customizations of properties of the class. */
    def propertyCustomizations = _propertyCustomizations
}
