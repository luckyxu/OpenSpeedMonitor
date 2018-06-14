package de.iteratec.osm.result.dao.query.projector

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.MeasurandTrim
import de.iteratec.osm.result.dao.ProjectionProperty

class MeasurandRawDataProjector implements EventResultProjector {
    @Override
    Closure generateSelectedMeasurandProjectionFor(List<SelectedMeasurand> measurands, Set<ProjectionProperty> baseProjections) {
        return {
            projections {
                measurands.each {
                    property it.databaseRelevantName, it.databaseRelevantName
                }
                baseProjections.each {ProjectionProperty pp ->
                    property pp.dbName, pp.alias
                }
            }
        }
    }

    @Override
    List<Closure> buildTrims(List<SelectedMeasurand> measurands, List<MeasurandTrim> trims) {
        List<Closure> trimClosures = []
        trims = trims?:[]
        trims.each{MeasurandTrim trim->
            measurands.each{SelectedMeasurand selectedMeasurand ->
                if (selectedMeasurand.measurandGroup == trim.measurandGroup){
                    trimClosures.add({
                        "${trim.qualifier.getGormSyntax()}" "${selectedMeasurand.databaseRelevantName}", trim.value
                    })
                }
            }
        }
        return trimClosures
    }
}
