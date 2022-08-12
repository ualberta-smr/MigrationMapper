from operator import le
from pprint import pprint
import pandas as pd
from sqlalchemy import create_engine
import numpy as np

db_connection_str = 'mysql+pymysql://root:root@localhost/migrationmapper'

lib_name_mapping= {
    'slf4j-api': 'slf4j',
    'com.fasterxml.jackson.core:jackson:xxx': 'jackson',
    'com.google.collections:google-collect:xxx' : 'google-collect',
    'com.googlecode.json-simple:json:xxx': 'json-simple',
    'commons-lang:commons-lang:xxx': 'commons-lang',
    'org.slf4j:slf4j:xxx': 'slf4j',
    'javax.faces:jsf-api:xxx': 'jsf-api',
    'org.glassfish:javax.faces:xxx': 'javax.faces',
    'org.hibernate:hibernate-core:xxx': 'hibernate-core',
    'com.fasterxml.jackson.core:jackson-databind:xxx': 'jackson',
    'org.hibernate.javax.persistence:hibernate-jpa-2.0-api:xxx': 'hibernate',
    'com.google.guava:guava:xxx': 'guava',
    'org.slf4j:slf4j-api:xxx': 'slf4j',
    'org.mortbay.jetty:jetty:xxx': 'jetty',
    'org.apache.httpcomponents:httpclient:xxx': 'httpclient',
    'org.scribe:scribe:xxx': 'scribe',
    'org.apache.logging.log4j:log4j-core:xxx': 'log4j',
    'org.apache.logging.log4j:log4j-api:xxx': 'log4j',
    'org.apache.httpcomponents:httpclient-osgi:xxx': 'httpclient',
    'com.amazonaws:aws-java-sdk:xxx': 'aws-java-sdk',
    'org.eclipse.jetty:jetty-servlet:xxx': 'jetty',
    'org.mockito:mockito-core:xxx': 'mockito',
    'org.mockito:mockito-all:xxx': 'mockito',
    'org.eclipse.rdf4j:rdf4j-rio-api:xxx': 'rdf4j',
    'org.eclipse.rdf4j:rdf4j-model:xxx': 'rdf4j',
    'org.eclipse.rdf4j:rdf4j-sail-memory:xxx': 'rdf4j',
    'org.eclipse.rdf4j:rdf4j-repository-api:xxx': 'rdf4j',
    'org.eclipse.rdf4j:rdf4j:xxx': 'rdf4j',
    'org.ops4j.pax.runner:pax-runner-no-jcl:xxx'
    'org.apache.httpcomponents:httpcore:xxx': 'httpclient',
    'org.easymock:easymock:xxx': 'easymock',
    'org.mockito:mockito:xxx': 'mockito',
    'com.google.code.gson:gson:xxx': 'gson',
    'junit:junit:xxx': 'junit',
    'org.testng:testng:xxx': 'testng',
    'org.codehaus.jackson:jackson-mapper-asl:xxx': 'jackson',
    'com.google.collections:google-collections:xxx': 'google-collections',
    'commons-logging:commons-logging:xxx': 'commons-logging',
    'jetty:org.mortbay.jetty:xxx': 'jetty',
    'com.sun.grizzly:grizzly-servlet-webserver:xxx': 'grizzly',
    'commons-httpclient:commons-httpclient:xxx': 'commons-httpclient',
    'jetty:org.mortbay.jetty-jdk1.2:xxx': 'jetty',
    'org.json:json:xxx': 'json',
    'net.java.dev.jets3t:jets3t:xxx': 'jets3t',
    'org.glassfish.grizzly:grizzly-http-servlet:xxx': 'grizzly',
    'easymock:easymock:xxx': 'easymock',
    'org.openrdf.sesame:sesame-rio-api:xxx': 'sesame',
    'org.openrdf.sesame:sesame-model:xxx': 'sesame',
    'org.openrdf.sesame:sesame-rio-turtle:xxx': 'sesame',
    'org.openrdf.sesame:sesame-sail-memory:xxx': 'sesame',
    'org.openrdf.sesame:sesame-repository-api:xxx': 'sesame',
    'org.openrdf.sesame:sesame:xxx':'sesame',
    'log4j:log4j:xxx': 'log4j',
}

def replace_with_simple_name(lib_name:str):
    if lib_name in lib_name_mapping:
        return lib_name_mapping[lib_name]
    return lib_name

def process_results(results, process_items = True):
    fl = results["FromLibrary"].apply(lambda ln: replace_with_simple_name(ln))
    tl = results["ToLibrary"].apply(lambda ln: replace_with_simple_name(ln))
    # if process_items:
    #     # fl = fl.apply(lambda lib: lib.split(':')[-3])
    #     # tl = tl.apply(lambda lib: lib.split(':')[-3])
    #     fl = fl.apply(lambda lib: lib[:-4])
    #     tl = tl.apply(lambda lib: lib[:-4])
    pairs = pd.unique(fl + "   " + tl)
    full = fl + "..." + tl + "..." + results["FromCode"].apply(lambda code: code.replace('\n', ' ').strip()) + "..." +  results["ToCode"].apply(lambda code: code.replace('\n', ' ').strip())
    #print(*pairs, sep="\n")
    #print()
    print(len(pairs), len(full))
    return pairs,  full


def main():
    
    gt_pairs, gt_mappings = process_results(pd.read_html(open("reproduction/groundTruth_icpc2019.html", "r"))[0])
    their_pairs, their_mappings = process_results(pd.read_html(open("reproduction/results_icpc2019.html", "r"))[0], False)
    db_connection = create_engine(db_connection_str)
    q = "select * from migrationmappingview"
    our_pairs, our_mappings = process_results(pd.read_sql(q, con=db_connection))

    #print("ground truth", len(gt_pairs))
    #print("their", len(their_pairs))
    #print("our", len(our_pairs))

    #print("In ground truth, but not in ours:")
    #print(np.setdiff1d(gt_pairs, our_pairs))


    gt_not_found_in_ours = np.setdiff1d(gt_mappings, our_mappings)
    
    # print("\n\n".join( gt_not_found_in_ours.tolist()))
    print(f"{len(gt_not_found_in_ours)} out of {len(gt_mappings)} mappings not found")

    # our_libs = pd.unique(pd.concat([our_all["FromLibrary"], our_all["ToLibrary"]]))
    # print(our_libs)
    


if __name__ == "__main__":
    main()

# create view migrationmappingview as select * from migrationmapping mm left join migrationrules mr on mm.MigrationRuleID = mr.ID;