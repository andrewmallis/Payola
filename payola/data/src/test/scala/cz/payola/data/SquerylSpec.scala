package cz.payola.data

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import cz.payola.domain.entities.plugins.concrete.data.SparqlEndpoint
import cz.payola.domain.entities.plugins.concrete.query._
import cz.payola.domain.entities.plugins.concrete._
import cz.payola.domain.entities.privileges._
import cz.payola.domain.entities.plugins.DataSource
import collection.immutable
import cz.payola.domain.entities.settings.OntologyCustomization

class SquerylSpec extends TestDataContextComponent("squeryl", false) with FlatSpec with ShouldMatchers
{
    // Users
    val u1 = new cz.payola.domain.entities.User("HS")
    val u2 = new cz.payola.domain.entities.User("ChM")
    val u3 = new cz.payola.domain.entities.User("JH")
    val u4 = new cz.payola.domain.entities.User("OK")
    val u5 = new cz.payola.domain.entities.User("OH")

    // Groups
    val g1 = new cz.payola.domain.entities.Group("group1", u1)
    val g2 = new cz.payola.domain.entities.Group("group2", u2)
    val g3 = new cz.payola.domain.entities.Group("group3", u3)
    val g4 = new cz.payola.domain.entities.Group("group4", u5)
    val g5 = new cz.payola.domain.entities.Group("group5", u5)

    // Plugins
    val sparqlEndpointPlugin = new SparqlEndpoint
    val concreteSparqlQueryPlugin = new ConcreteSparqlQuery
    val projectionPlugin = new Projection
    val selectionPlugin = new Selection
    val typedPlugin = new Typed
    val join = new Join
    val unionPlugin = new Union

    val plugins = List(
        sparqlEndpointPlugin,
        concreteSparqlQueryPlugin,
        projectionPlugin,
        selectionPlugin,
        typedPlugin,
        join,
        unionPlugin
    )

    "Schema" should "be created" in {
        schema.recreate
    }

    "Users" should "be persited, loaded and managed by UserRepository" in {
        schema.wrapInTransaction { persistUsers }
    }

    private def persistUsers {
        val user = userRepository.persist(u1)
        assert(userRepository.persist(u2) != null)
        assert(userRepository.persist(u3) != null)
        assert(userRepository.persist(u4) != null)
        assert(userRepository.persist(u5) != null)

        // Update test
        user.email = "email"
        user.password = "password"
        var u = userRepository.persist(user)
        assert(u.email == user.email)
        assert(u.password == user.password)
        assert(u.name == u1.name)

        u = userRepository.getById(user.id).get
        assert(u.id == u1.id)
        assert(u.name == u1.name)
        assert(u.password == user.password)
        assert(u.email == user.email)

        assert(userRepository.getAllWithNameLike("h")(0).id == u2.id)
        assert(userRepository.getAllWithNameLike("J")(0).id == u3.id)
        assert(userRepository.getAllWithNameLike("K")(0).id == u4.id)
        assert(userRepository.getAllWithNameLike("H").size == 3)
        assert(userRepository.getAllWithNameLike(user.name)(0).id == u1.id)
        assert(userRepository.getAllWithNameLike("invalid name").size == 0)
        assert(userRepository.getByCredentials(user.name, user.password).get.id == u1.id)
        assert(userRepository.getByCredentials("invalid", "credientals") == None)
    }

    "Groups" should "be persisted, loaded and managed by GroupRepository" in {
        schema.wrapInTransaction { persistGroups }
    }

    private def persistGroups {
        groupRepository.persist(g1)
        groupRepository.persist(g2)
        groupRepository.persist(g3)
        groupRepository.persist(g4)
        groupRepository.persist(g5)

        var g = groupRepository.getById(g1.id)
            assert(g != None)
            assert(g.get.id == g1.id)
            assert(g.get.name == g1.name)
            assert(g.get.owner.id == u1.id)

        g = groupRepository.getById(g2.id)
            assert(g != None)
            assert(g.get.id == g2.id)
            assert(g.get.name == g2.name)
            assert(g.get.owner.id == u2.id)

        var user = userRepository.getById(u1.id).get
            assert(user.ownedGroups.size == 1)
        user = userRepository.getById(u4.id).get
            assert(user.ownedGroups.size == 0)
        user = userRepository.getById(u5.id).get
            assert(user.ownedGroups.size == 2)
    }

    "Groups" should "maintain members collection" in {
        schema.wrapInTransaction { persistGroupMemberships }
    }

    private def persistGroupMemberships {
        val group1 = groupRepository.getById(g1.id).get
        val group2 = groupRepository.getById(g2.id).get
        val group3 = groupRepository.getById(g3.id).get
        val group4 = groupRepository.getById(g4.id).get
        val group5 = groupRepository.getById(g5.id).get

        group2.addMember(userRepository.getById(u1.id).get)
        group1.addMember(userRepository.getById(u2.id).get)
        group1.addMember(userRepository.getById(u3.id).get)
        group2.addMember(userRepository.getById(u4.id).get)
        group2.addMember(userRepository.getById(u5.id).get)

            assert(group1.members.size == 2)
            assert(group2.members.size == 3)
            assert(group3.members.size == 0)
            assert(group4.members.size == 0)
            assert(group5.members.size == 0)
    }

    "Plugins" should "be persisted with their parameters by PluginRepository" in {
        schema.wrapInTransaction { persistPlugins }
    }

    private def persistPlugins {
        unionPlugin.owner = Some(u1)
        unionPlugin.isPublic = true
        
        for (p <- plugins) {
            val p1 = pluginRepository.persist(p)
                assert(p1.id == p.id)

            val p2 = pluginRepository.getByName(p.name).get
                assert(p1.id == p2.id)
                assert(p2.parameters.size == p.parameters.size)
                assert(p1.parameters.size == p.parameters.size)

            // assert all parameters have proper IDs
            for (param <- p1.parameters) {
                assert(p.parameters.find(_.id == param.id).get.name == param.name)
                assert(p.parameters.find(_.id == param.id).get.defaultValue == param.defaultValue)
            }

            // assert all parameters have proper IDs
            for (param <- p2.parameters) {
                assert(p.parameters.find(_.id == param.id).get.name == param.name)
                assert(p.parameters.find(_.id == param.id).get.defaultValue == param.defaultValue)
            }
        }

        // Assert instantiation
        val endPoint = pluginRepository.getById(sparqlEndpointPlugin.id).get
        assert(endPoint.owner == None)
        for( param <- endPoint.parameters) {
            assert(sparqlEndpointPlugin.parameters.find(_.id == param.id).get.name == param.name)
            assert(sparqlEndpointPlugin.parameters.find(_.id == param.id).get.defaultValue == param.defaultValue)
        }

        // getCount is not used on purpose to test instantiation:
        assert(pluginRepository.getAll().size == plugins.size)
        assert(pluginRepository.getById(unionPlugin.id).get.owner == Some(u1))
        assert(pluginRepository.getById(unionPlugin.id).get.isPublic == unionPlugin.isPublic)
        assert(pluginRepository.getById(unionPlugin.id).get.owner.get.ownedPlugins.size == 1)
    }

    "Analysis" should "be stored/updated/loaded by AnalysisRepository" in {
        schema.wrapInTransaction { persistAnalyses }
    }

    private def persistAnalyses {
        val user = userRepository.getById(u1.id).get
        val count = analysisRepository.getCount
        val a = new cz.payola.domain.entities.Analysis(
            "Cities with more than 2M habitants with countries " + count,
            Some(user)
        )
        
        a.isPublic = true
        a.description = "description"

        println("      defining analysis")
        val citiesFetcher = sparqlEndpointPlugin.createInstance()
            .setParameter("EndpointURL", "http://dbpedia.org/sparql")
        val citiesTyped = typedPlugin.createInstance().setParameter("TypeURI", "http://dbpedia.org/ontology/City")
        val citiesProjection = projectionPlugin.createInstance().setParameter("PropertyURIs", List(
            "http://dbpedia.org/ontology/populationDensity", "http://dbpedia.org/ontology/populationTotal"
        ).mkString("\n"))
        val citiesSelection = selectionPlugin.createInstance().setParameter(
            "PropertyURI", "http://dbpedia.org/ontology/populationTotal"
        ).setParameter(
            "Operator", ">"
        ).setParameter(
            "Value", "2000000"
        )

        citiesFetcher.description = "fetch"
        citiesFetcher.isEditable = true

        // Try that defined analysis can be persisted
        a.addPluginInstances(citiesFetcher, citiesTyped, citiesProjection, citiesSelection)
        a.addBinding(citiesFetcher, citiesTyped)
        a.addBinding(citiesTyped, citiesProjection)
        a.addBinding(citiesProjection, citiesSelection)

        // Persist defined analysis
        println("      persisting defined analysis")
        val analysis = analysisRepository.persist(a)

            assert(analysisRepository.getById(analysis.id).isDefined)
            assert(analysis.owner.get.id == user.id)
            assert(user.ownedAnalyses.size == count + 1)
            assert(analysis.isPublic == a.isPublic)
            assert(analysis.description == a.description)

            // Asset all is persisted
            assert(analysis.pluginInstances.size == a.pluginInstances.size)
            assert(analysis.pluginInstances.size > 0)
            assert(analysis.pluginInstanceBindings.size == a.pluginInstanceBindings.size)
            assert(analysis.pluginInstanceBindings.size > 0)

        val countriesFetcher = sparqlEndpointPlugin.createInstance()
            .setParameter("EndpointURL", "http://dbpedia.org/sparql")
        val countriesTyped = typedPlugin.createInstance().setParameter("TypeURI", "http://dbpedia.org/ontology/Country")
        val countriesProjection = projectionPlugin.createInstance().setParameter("PropertyURIs", List(
            "http://dbpedia.org/ontology/areaTotal"
        ).mkString("\n"))

            analysis.addPluginInstances(countriesFetcher, countriesTyped, countriesProjection)
            analysis.addBinding(countriesFetcher, countriesTyped)
            analysis.addBinding(countriesTyped, countriesProjection)

        val citiesCountriesJoin = join.createInstance().setParameter(
            "JoinPropertyURI", "http://dbpedia.org/ontology/country"
        ).setParameter(
            "IsInner", false
        )

            analysis.addPluginInstances(citiesCountriesJoin)
            analysis.addBinding(citiesSelection, citiesCountriesJoin, 0)
            analysis.addBinding(countriesProjection, citiesCountriesJoin, 1)

        println("      asserting persisted analysis")

        // Get analysis from DB
        val persistedAnalysis = analysisRepository.getById(analysis.id).get
            assert(persistedAnalysis.pluginInstances.size == analysis.pluginInstances.size)
            assert(persistedAnalysis.pluginInstances.size == 8)
            assert(persistedAnalysis.pluginInstanceBindings.size == analysis.pluginInstanceBindings.size)
            assert(persistedAnalysis.pluginInstanceBindings.size == 7)
            assert(persistedAnalysis.owner.get.id == user.id)

        // Assert persisted plugins instances
        val pluginInstances = List(
            citiesFetcher,
            citiesTyped,
            citiesProjection,
            citiesSelection,
            countriesFetcher,
            countriesTyped,
            countriesProjection,
            citiesCountriesJoin
        )

        // Assert eagerly-loaded relations (by analysis) to plugins and parameters
        for (pi <- pluginInstances) {
            val pi2 = persistedAnalysis.pluginInstances.find(_.id == pi.id)
                assert(pi2.isDefined)
                assert(pi2.get.id == pi.id)
                assert(pi2.get.plugin.id == pi.plugin.id)
                assert(pi2.get.parameterValues.size == pi.parameterValues.size)

            // assert all parameters have proper IDs
            for (paramValue <- pi2.get.parameterValues) {
                assert(pi.parameterValues.find(_.id == paramValue.id).get.parameter.id == paramValue.parameter.id)
                assert(pi.parameterValues.find(_.id == paramValue.id).get.value == paramValue.value)
            }
        }
    }

    "DataSources" should "be updated/stored by DataSourceRepository" in {
        schema.wrapInTransaction { persistDataSources }
    }

    private def persistDataSources {
        println("Persisting datasources")

        val ds1 = new DataSource("Cities", None, sparqlEndpointPlugin, immutable.Seq(
            sparqlEndpointPlugin.parameters(0).asInstanceOf[cz.payola.domain.entities.plugins.parameters.StringParameter]
                .createValue("http://dbpedia.org/ontology/Country")))
        val ds2 = new DataSource("Countries", Some(u2), sparqlEndpointPlugin, immutable.Seq(
            sparqlEndpointPlugin.parameters(0).asInstanceOf[cz.payola.domain.entities.plugins.parameters.StringParameter]
                .createValue("http://dbpedia.org/ontology/City")))
        val ds3 = new DataSource("Countries2", Some(u3), sparqlEndpointPlugin, immutable.Seq(
            sparqlEndpointPlugin.parameters(0).asInstanceOf[cz.payola.domain.entities.plugins.parameters.StringParameter]
                .createValue("http://dbpedia.org/ontology/City")))

        ds1.isPublic = true
        ds2.isPublic = true
        ds3.isPublic = true
        ds1.description = "desc"
        ds1.isEditable = true

        val ds1_db = dataSourceRepository.persist(ds1)
        val ds2_db = dataSourceRepository.persist(ds2)
        val ds3_db = dataSourceRepository.persist(ds3)

            assert(ds1.id == ds1_db.id)
            assert(ds2.id == ds2_db.id)
            assert(ds3.id == ds3_db.id)

            assert(ds1.parameterValues.size == ds1_db.parameterValues.size)
            assert(ds2.parameterValues.size == ds2_db.parameterValues.size)
            assert(ds3.parameterValues.size == ds3_db.parameterValues.size)

            assert(ds1_db.isPublic == ds1.isPublic)
            assert(ds1_db.description == ds1.description)
            assert(ds1_db.isEditable == ds1.isEditable)
            assert(u2.id == ds2_db.owner.get.id)
            assert(u3.id == ds3_db.owner.get.id)

            assert(dataSourceRepository.getAllPublic.size == 3)
            assert(dataSourceRepository.getCount == 3)
            assert(ds3_db.owner.get.ownedDataSources.size == 1)
        
            assert(ds3_db.plugin.id == sparqlEndpointPlugin.id)
            assert(ds3_db.parameterValues(0).parameter.id == sparqlEndpointPlugin.parameters(0).id)
    }

    "Privileges" should "be granted and persisted properly" in {
        schema.wrapInTransaction { persistPrivileges }
    }

    private def persistPrivileges {
        val a1 = analysisRepository.getAll()(0)
        val ds1 = dataSourceRepository.getAll()(0)
        val ds2 = dataSourceRepository.getAll()(1)
        val user1 = userRepository.getById(u1.id).get
        val user2 = userRepository.getById(u2.id).get
        val group1 = groupRepository.getById(g1.id).get
        val p = pluginRepository.getById(sparqlEndpointPlugin.id).get

        val p1 = new AccessAnalysisPrivilege(user1, user2, a1)
        val p2 = new AccessDataSourcePrivilege(user2, user1, ds2)
        val p3 = new AccessDataSourcePrivilege(user1, group1, ds1)
        val p4 = new AccessAnalysisPrivilege(user2, group1, a1)
        val p5 = new UsePluginPrivilege(user2, group1, p)
        val p6 = new UsePluginPrivilege(user2, user1, p)

        user2.grantPrivilege(p1)
        user1.grantPrivilege(p2)
        group1.grantPrivilege(p3)
        group1.grantPrivilege(p4)
        group1.grantPrivilege(p5)
        user1.grantPrivilege(p6)

        val p1_db = privilegeRepository.getById(p1.id).get

        assert(p1_db.id == p1.id)
        assert(p1_db.granter == p1.granter)
        assert(p1_db.grantee == p1.grantee)
        assert(p1_db.obj == p1.obj)
        
        assert(privilegeRepository.getById(p2.id).get.granter == p2.granter)
        assert(privilegeRepository.getById(p3.id).get.grantee == p3.grantee)
        assert(privilegeRepository.getById(p5.id).get.obj == p5.obj)
        assert(privilegeRepository.getByIds(List(p4.id, p6.id)).size == 2)

        assert(privilegeRepository.getCount == 6)
        assert(user1.grantedDataSources.size == 1)
        assert(user2.grantedAnalyses.size == 1)
        assert(user1.grantedPlugins.size == 1)
        assert(group1.grantedDataSources.size == 1)
        assert(group1.grantedAnalyses.size == 1)
        assert(group1.grantedPlugins.size == 1)

        assert(privilegeRepository.getAllGrantedTo(List(group1.id), classOf[UsePluginPrivilege]).size == 1)
        assert(privilegeRepository.getByGrantee(user1.id).size == 2)
    }

    "Pagionation" should "work" in {
        schema.wrapInTransaction { testPagination }
    }

    private def testPagination {
        assert(userRepository.getAll(Some(new PaginationInfo(2,1))).size == 1)
        assert(userRepository.getAll(Some(new PaginationInfo(2,4))).size == 3)
        assert(userRepository.getAll(Some(new PaginationInfo(5,1))).size == 0)
        assert(userRepository.getAll(Some(new PaginationInfo(4,0))).size == 0)

        assert(groupRepository.getAll().size == 5)
        assert(groupRepository.getAll(Some(new PaginationInfo(1, 2))).size == 2)
        assert(groupRepository.getAll(Some(new PaginationInfo(2, 5))).size == 3)
        assert(groupRepository.getAll(Some(new PaginationInfo(5, 1))).size == 0)
        assert(groupRepository.getAll(Some(new PaginationInfo(4, 0))).size == 0)
    }

    "Customizations" should "be persisted" in {
        schema.wrapInTransaction { persistCustomizations }
    }

    private def persistCustomizations {
        val url = "http://opendata.cz/pco/public-contracts.xml"
        val customization = OntologyCustomization.empty(url, "Name1", None)
        customization.isPublic = true
        val ownedCustomization = OntologyCustomization.empty(url, "Name2", Some(u1))

        val c1 = ontologyCustomizationRepository.persist(customization)
        val c2 = ontologyCustomizationRepository.persist(ownedCustomization)
        
        val cc1 = ownedCustomization.classCustomizations(0)
        cc1.radius = 1
        cc1.fillColor = "grey"
        cc1.glyph = Some('g')

        val pp1 = cc1.propertyCustomizations(0)
        pp1.strokeColor = "blue"
        pp1.strokeWidth = "2"
        
        assert(c1.id == customization.id)
        assert(c1.isPublic == customization.isPublic)
        assert(c2.id == ownedCustomization.id)
        assert(c2.owner.get.id == u1.id)
        assert(c2.owner.get.ownedOntologyCustomizations.size == 1)

        // Assert eager-loading
        val c3 = ontologyCustomizationRepository.getById(ownedCustomization.id).get
            assert(c3.owner == Some(u1))
            assert(c3.name == ownedCustomization.name)
            assert(c3.ontologyURL == ownedCustomization.ontologyURL)
            assert(c3.classCustomizations.size == ownedCustomization.classCustomizations.size)
        
        for (cc <- ownedCustomization.classCustomizations){
            val persistedCc = c3.classCustomizations.find(_.id == cc.id).get
                assert(persistedCc.uri == cc.uri)
                assert(persistedCc.fillColor == cc.fillColor)
                assert(persistedCc.radius == cc.radius)
                assert(persistedCc.glyph == cc.glyph)
                assert(cc.propertyCustomizations.size == persistedCc.propertyCustomizations.size)

            for (pc <- cc.propertyCustomizations){
                val persistedPc = persistedCc.propertyCustomizations.find(_.id == pc.id).get
                    assert(persistedPc.uri == pc.uri)
                    assert(persistedPc.strokeWidth == pc.strokeWidth)
                    assert(persistedPc.strokeColor == pc.strokeColor)
            }
        }
    }

    "Entities" should "be removed with their related entities" in {
        schema.wrapInTransaction { testCascadeDeletes }
    }

    private def testCascadeDeletes {
        val analysisCount = analysisRepository.getAll().size
        val pluginsCount = pluginRepository.getAll().size

        // Create another analysis in DB
        persistAnalyses

        assert(analysisRepository.getCount == analysisCount + 1)
        assert(pluginRepository.getCount == pluginsCount)

        // Remove one analysis
        assert(analysisRepository.removeById(analysisRepository.getAll()(0).id) == true)

        // One analysis and half of plugin instances are gone
        assert(analysisRepository.getCount == analysisCount)
        assert(pluginRepository.getCount == pluginsCount)

        val analysis = analysisRepository.getAll()(0)

        // Remove all plugins
        for (p <- plugins) {
            assert(pluginRepository.removeById(p.id) == true)
        }

        // Only (empty) analysis is left
        assert(analysisRepository.getCount == analysisCount)
        assert(pluginRepository.getCount == 0)

        // Assert nothing left for analysis
        assert(analysis.pluginInstances.size == 0)
        assert(analysis.pluginInstanceBindings.size == 0)

        // Remove user and all his entities
        assert(userRepository.removeById(u1.id))
        assert(userRepository.removeById(u2.id))
        assert(userRepository.removeById(u3.id))
        assert(userRepository.removeById(u4.id))
        assert(userRepository.removeById(u5.id))
    }
}
