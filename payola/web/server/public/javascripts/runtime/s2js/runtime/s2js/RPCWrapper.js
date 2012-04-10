goog.provide('s2js.RPCWrapper');
goog.require('s2js.RPCException');
s2js.RPCWrapper.callSync = function(procedureName, parameters, parameterTypes) {
var self = this;


        var url = "/RPC";

        var request = XMLHttpRequest  ? new XMLHttpRequest : new ActiveXObject('Msxml2.XMLHTTP');
        request.open("POST", url, false);

        //Send the proper header information along with the request
        request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");

        if (parameters.length > 0)
        {
            var params = this.buildHttpQuery(parameters);
            request.send("method="+procedureName+"&"+params);
        }else{
            request.send("method="+procedureName);
        }

        if (request.readyState==4 && request.status==200)
        {
            var refQueue = [];
            var objectRegistry = {};
            var instance = this.deserialize(eval("("+request.responseText+")"), objectRegistry, refQueue);

            for (var k in refQueue)
            {
                refQueue[k].obj[refQueue[k].key] = objectRegistry[refQueue[k].refID];
            }

            return instance;
        }else{
            throw new s2js.RPCException("RPC call exited with status code "+request.status);
        }
    };
s2js.RPCWrapper.buildHttpQuery = function(params) {
var self = this;

        var args = '';
        if (Object.prototype.toString.call(params) === '[object Array]') {
                var arr = [];
                for (arg in params) {
                        arr.push(encodeURIComponent(arg) + '=' + encodeURIComponent(params[arg]));
                }
                args = arr.join('&');
        }

        return args;
    };
s2js.RPCWrapper.deserialize = function(obj, objectRegistry, refQueue) {
var self = this;
if (typeof(objectRegistry) === 'undefined') { objectRegistry = null; }
if (typeof(refQueue) === 'undefined') { refQueue = null; }


        // check if the deserialized object is of type Object
        if (Object.prototype.toString.call(obj) !== '[object Object]')
        {
            // if not, it is a scalar or an array, so return it
            return obj;
        }

        // init object reference registry (reuse or create)
        objectRegistry = objectRegistry || {};

        // handle collections carefully
        if (typeof(obj.__arrayClass__) !== "undefined")
        {
            return this.deserializeArrayClass(obj, objectRegistry, refQueue);
        }

        // a "typical" object, get its className
        var clazz = obj.__class__;

        // if it doesn't have a className set, it is a anonymous object (or bug in serialization)
        if (typeof(clazz) === 'undefined')
        {
            // registrer based on objectID into the registry and return
            objectRegistry[obj.__objectID__] = obj;
            return obj;
        }

        var result = this.checkDefinedAndMakeInstance(clazz);
        if (result == null)
        {
            return obj;
        }

        this.deserializeProperties(obj, result, objectRegistry, refQueue);

        // assign into the registry
        objectRegistry[obj.__objectID__] = result;
        return result;

    };
s2js.RPCWrapper.deserializeProperties = function(obj, result, objectRegistry, refQueue) {
var self = this;

        // deserialize via recursion, but be aware of references, which are probably not yet created,
        // so add the "set reference" request into a queue to make it later
        for (var key in obj)
        {
            // is it a reference?
            if ((Object.prototype.toString.call(obj[key]) === '[object Object]') && (typeof(obj[key].__ref__) !==
            "undefined"))
            {
                // push the setRef task into the queue
                refQueue.push({
                    "obj": result,
                    "key": key,
                    "refID": obj[key].__ref__
                });
                continue;
            }

            // skip properties beginning with "__"
            if (key.match(/^__/)) continue;

            // deserialize the object right now
            result[key] = this.deserialize(obj[key], objectRegistry, refQueue);
        }
    };
s2js.RPCWrapper.deserializeArrayClass = function(obj, objectRegistry, refQueue) {
var self = this;
if (typeof(objectRegistry) === 'undefined') { objectRegistry = null; }
if (typeof(refQueue) === 'undefined') { refQueue = null; }

        // create an instance of the collection class
        var instance = this.checkDefinedAndMakeInstance(obj.__arrayClass__);
        if (instance == null)
        {
            return obj;
        }

        // deserialize members of the collection and add them to the instance
        for (var i = 0; i < obj.__value__.length; i++) {
            instance.$plus$eq(this.deserialize(obj.__value__[i], objectRegistry, refQueue));
        }

        // register the object into the registry
        objectRegistry[obj.__objectID__] = instance;

        // done
        return instance;
    };
s2js.RPCWrapper.checkDefinedAndMakeInstance = function(className) {
var self = this;


        var namespaces = className.split(".");
        var fqdn = "";

        for (var k in namespaces)
        {
            if (k > 0){
                fqdn += ".";
            }

            fqdn += namespaces[k];

            // check if the type is already loaded
            if (eval("typeof("+fqdn+")") === 'undefined')
            {
                // if not, load it
                //TODO
                window.alert("Should load "+className+" (undefined "+ fqdn+")");
                return null;
            }
        }

        // make an instance of the desired type
        var result = eval("new "+className+"()");
        return result;
    };
s2js.RPCWrapper.__class__ = new s2js.Class('s2js.RPCWrapper', []);
