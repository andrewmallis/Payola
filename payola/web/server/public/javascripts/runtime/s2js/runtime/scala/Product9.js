goog.provide('scala.Product9');
goog.require('scala.IndexOutOfBoundsException');
goog.require('scala.Product');
goog.require('scala.Some');
scala.Product9 = function() {
var self = this;
};
goog.inherits(scala.Product9, scala.Product);
scala.Product9.prototype.productArity = function() {
var self = this;
return 9;
};
scala.Product9.prototype.productElement = function(n) {
var self = this;
return (function($selector$1) {
if ($selector$1 === 0) {
return self._1();
}
if ($selector$1 === 1) {
return self._2();
}
if ($selector$1 === 2) {
return self._3();
}
if ($selector$1 === 3) {
return self._4();
}
if ($selector$1 === 4) {
return self._5();
}
if ($selector$1 === 5) {
return self._6();
}
if ($selector$1 === 6) {
return self._7();
}
if ($selector$1 === 7) {
return self._8();
}
if ($selector$1 === 8) {
return self._9();
}
if (true) {
return (function() {
throw new scala.IndexOutOfBoundsException(n.toString());
})();
}
})(n);
};
scala.Product9.prototype.__class__ = new s2js.Class('scala.Product9', [scala.Product]);
scala.Product9.unapply = function(x) {
var self = this;
return new scala.Some(x);
};
scala.Product9.__class__ = new s2js.Class('scala.Product9', []);
