'''
Created on May 7, 2014

@author: xgeoraj
'''

from xml.dom.minidom import parse
from xml.dom.minidom import Node

import sys
script_name = sys.argv[1]
mo_name = sys.argv[2]
modifier_name = sys.argv[3]
number_name = sys.argv[4]

id_name = sys.argv[5]


'''print script_name'''
bcg_import_file = "/var/opt/ericsson/nms_umts_wran_bcg/files/export/"+script_name
'''print bcg_import_file'''
modified_file = "/var/opt/ericsson/nms_umts_wran_bcg/files/import/"+script_name
'''print modified_file'''
DomTree = parse(bcg_import_file)
xmlTree = DomTree.documentElement

if mo_name == "LocationArea":
    first_tag = "xn:SubNetwork"
if mo_name == "ServiceArea" or mo_name == "RoutingArea":
    first_tag = "xn:VsDataContainer"
if mo_name == "Aal2Ap":
    first_tag = "xn:ManagedElement"
    second_tag = "un:" + mo_name
if mo_name == "NodeSynch":
    first_tag = "un:RncFunction"
    second_tag = "un:" + mo_name
if mo_name == "NbapCommon":
    first_tag = "xn:ManagedElement"
    second_tag = "un:" + mo_name
if mo_name == "Aal0TpVccTp":
    first_tag = "xn:ManagedElement"
    second_tag = "un:" + mo_name
if mo_name == "UtranRelation" or mo_name == "Fach":
    first_tag = "un:RncFunction"
    second_tag = "un:" + mo_name
if mo_name == "GsmRelation":
    first_tag = "un:RncFunction"
    second_tag = "gn:" + mo_name
if mo_name == "UtranCell":
    first_tag = "un:RncFunction"
    second_tag = "un:" + mo_name
if mo_name == "GeranCellRelation":
    first_tag = "xn:ManagedElement"
    second_tag = "un:" + mo_name
if mo_name == "ExternalGsmCell":
    first_tag = "xn:SubNetwork"
    second_tag = "gn:" + mo_name
if mo_name == "ExternalUtranCell":
    first_tag = "xn:SubNetwork"
    second_tag = "un:" + mo_name
if mo_name == "CellSleepFunction":
    first_tag = "xn:SubNetwork"
    second_tag = "un:" + mo_name
if mo_name == "Rcs":
    first_tag = "xn:SubNetwork"
    second_tag = "un:" + mo_name
if mo_name == "Router":
    first_tag = "xn:SubNetwork"
    second_tag = "un:" + mo_name
if mo_name == "Ptp":
    first_tag = "xn:SubNetwork"
    second_tag = "un:" + mo_name
if mo_name == "VlanPort":
    first_tag = "xn:SubNetwork"
    second_tag = "un:" + mo_name
if mo_name == "Paging":
    first_tag = "xn:SubNetwork"
    second_tag = "un:" + mo_name
if mo_name == "Bridge":
    first_tag = "xn:SubNetwork"
    second_tag = "un:" + mo_name
i = 0
j = 0

if mo_name == "LocationArea":
    subnetwork = xmlTree.getElementsByTagName("xn:VsDataContainer")[0]
    ExternalGsmCell_list = subnetwork.getElementsByTagName("xn:VsDataContainer")
    ExternalGsmCell_list.pop(0)
    for ExternalGsmCell in ExternalGsmCell_list:
        if "_" in (ExternalGsmCell.getAttribute("id")):
            subnetwork.removeChild(ExternalGsmCell)
    subnetwork = xmlTree.getElementsByTagName("xn:SubNetwork")[0].getElementsByTagName("xn:VsDataContainer")[1]
    ExternalGsmCell_list = subnetwork.getElementsByTagName("xn:VsDataContainer")
    ExternalGsmCell_list.pop(0)
    for ExternalGsmCell in ExternalGsmCell_list:
        subnetwork.removeChild(ExternalGsmCell)

if mo_name == "ServiceArea" or mo_name == "RoutingArea":
    newTree = xmlTree.getElementsByTagName("xn:SubNetwork")[0].getElementsByTagName("xn:VsDataContainer")[0].getElementsByTagName("xn:VsDataContainer")[0]
    innerVsDataContainer = newTree.getElementsByTagName("xn:VsDataContainer")[0]
    nodes = newTree.childNodes
    for node in nodes:
            type = node.nodeType
            if type == Node.ELEMENT_NODE:
                if node.hasAttributes():
                    if not node.attributes.get("id").value == innerVsDataContainer.attributes.get("id").value:
                        newTree.removeChild(node)

    nextTree = xmlTree.getElementsByTagName("xn:SubNetwork")[0].getElementsByTagName("xn:VsDataContainer")[1].getElementsByTagName("xn:VsDataContainer")[0]
    ExternalGsmCell_list =  nextTree.getElementsByTagName("xn:VsDataContainer")
    for ExternalGsmCell in ExternalGsmCell_list:
        if i >= 1:
            nextTree.removeChild(ExternalGsmCell)
        i = i + 1

if mo_name == "Aal2Ap":
    subnetwork = xmlTree.getElementsByTagName("xn:VsDataContainer")[1]
    aal2Ap_list =  subnetwork.getElementsByTagName("xn:VsDataContainer")
    aal2Ap_list.pop(0)
    for aal2ApMo in aal2Ap_list:
        subnetwork.removeChild(aal2ApMo)

if mo_name == "Aal2PathVccTp":
    newTree = xmlTree.getElementsByTagName("xn:ManagedElement")[0].getElementsByTagName("xn:VsDataContainer")[0]
    innerVsDataContainer = newTree.getElementsByTagName("xn:VsDataContainer")[0]
    nodes = newTree.childNodes
    for node in nodes:
            type = node.nodeType
            if type == Node.ELEMENT_NODE:
                if node.hasAttributes():
                    if not node.attributes.get("id").value == innerVsDataContainer.attributes.get("id").value:
                        newTree.removeChild(node)

if mo_name == "ExternalGsmCell" or mo_name == "ExternalUtranCell":
    subnetwork = xmlTree.getElementsByTagName(first_tag)[0]
    ExternalGsmCell_list =  subnetwork.getElementsByTagName(second_tag)
    ExternalGsmCell_list.pop()
    for ExternalGsmCell in ExternalGsmCell_list:
        subnetwork.removeChild(ExternalGsmCell)

if mo_name == "GeranCellRelation":
    newTree = xmlTree.getElementsByTagName("xn:ManagedElement")[0].getElementsByTagName("xn:VsDataContainer")[0]
    innerVsDataContainer = newTree.getElementsByTagName("xn:VsDataContainer")[0]
    nodes = newTree.childNodes
    for node in nodes:
            type = node.nodeType
            if type == Node.ELEMENT_NODE:
                if node.hasAttributes():
                    if not node.attributes.get("id").value == innerVsDataContainer.attributes.get("id").value:
                        newTree.removeChild(node)
    nextTree = xmlTree.getElementsByTagName("xn:ManagedElement")[0].getElementsByTagName("xn:VsDataContainer")[1].getElementsByTagName("xn:VsDataContainer")[0]
    ExternalGsmCell_list =  nextTree.getElementsByTagName("xn:VsDataContainer")
    for ExternalGsmCell in ExternalGsmCell_list:
        if i >= 1:
            nextTree.removeChild(ExternalGsmCell)
        i = i + 1

if mo_name == "NodeSynch":
    subnetwork = xmlTree.getElementsByTagName(first_tag)[0]
    NodeSynch_list =  subnetwork.getElementsByTagName("un:IubLink")
    NodeSynch_list.pop(0)
    for NodeSynchCell in NodeSynch_list:
        subnetwork.removeChild(NodeSynchCell)

if mo_name == "Fach":
    subnetwork = xmlTree.getElementsByTagName(first_tag)[0]
    Fach_list =  subnetwork.getElementsByTagName("un:UtranCell")
    Fach_list.pop(0)
    for FachCell in Fach_list:
        subnetwork.removeChild(FachCell)

if mo_name == "UtranCell":
    subnetwork = xmlTree.getElementsByTagName(first_tag)[0]
    ExternalGsmCell_list =  subnetwork.getElementsByTagName("un:UtranCell")
    for ExternalGsmCell in ExternalGsmCell_list:
        if ExternalGsmCell.getAttribute("id")=="TestBcg":
            ExternalGsmCell_list.pop(0);
        else:
            subnetwork.removeChild(ExternalGsmCell)

if  mo_name == "UtranRelation" or mo_name == "GsmRelation":
    subnetwork = xmlTree.getElementsByTagName(first_tag)[0]
    ExternalGsmCell_list =  subnetwork.getElementsByTagName("un:UtranCell")
    ExternalGsmCell_list.pop(0)
    for ExternalGsmCell in ExternalGsmCell_list:
        subnetwork.removeChild(ExternalGsmCell)
    if mo_name == "UtranRelation" or mo_name == "GsmRelation":
        subnetwork = xmlTree.getElementsByTagName("un:UtranCell")[0]
        ExternalGsmCell_list =  subnetwork.getElementsByTagName(second_tag)
        ExternalGsmCell_list.pop(0)
        for ExternalGsmCell in ExternalGsmCell_list:
            subnetwork.removeChild(ExternalGsmCell)

if mo_name == "UtranRelation" or mo_name == "UtranCell" or mo_name == "Aal0TpVccTp" or  mo_name == "ExternalGsmCell" or mo_name == "ExternalUtranCell" or mo_name == "Fach" or mo_name == "GsmRelation":
    utrancell_list =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName(second_tag)
    for utrancell in utrancell_list:
        utrancell.setAttribute('modifier', modifier_name)
    utrancell_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
    for utrancell_MO in utrancell_list_MO:
        utrancell_MO.setAttribute('modifier', modifier_name)

if mo_name == "NodeSynch":
    subnetwork = xmlTree.getElementsByTagName(first_tag)[0]
    NodeSynch_list =  subnetwork.getElementsByTagName("un:IubLink")
    for NodeSynchCell in NodeSynch_list:
        NodeSynchCell.setAttribute('modifier', modifier_name)

if mo_name == "Aal2Ap":
    Aal2Ap_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
    for Aal2Ap_MO in Aal2Ap_list_MO:
        if i ==2:
            Aal2Ap_MO.setAttribute('modifier', modifier_name)
		
            Aal2Ap_MO.setAttribute('id', id_name)
        i = i + 1

if mo_name == "CellSleepFunction":
    CellSleepFunction_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
    for CellSleepFunction_MO in CellSleepFunction_list_MO:
        if i ==2:
            CellSleepFunction_MO.setAttribute('modifier', modifier_name)
		
            CellSleepFunction_MO.setAttribute('id', id_name)
        i = i + 1

if mo_name == "Rcs":
    Rcs_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
    for Rcs_MO in Rcs_list_MO:
        if i ==1:
            Rcs_MO.setAttribute('modifier', modifier_name)
		
            Rcs_MO.setAttribute('id', id_name)
        i = i + 1
if mo_name == "Router":
    Router_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
    for Router_MO in Router_list_MO:
        if i ==1:
            Router_MO.setAttribute('modifier', modifier_name)
		
            Router_MO.setAttribute('id', id_name)
        i = i + 1
if mo_name == "Ptp":
    Ptp_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
    for Ptp_MO in Ptp_list_MO:
        if i ==1:
            Ptp_MO.setAttribute('modifier', modifier_name)
		
            Ptp_MO.setAttribute('id', id_name)
        i = i + 1
if mo_name == "Bridge":
    Bridge_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
    for Bridge_MO in Bridge_list_MO:
        if i ==1:
            Bridge_MO.setAttribute('modifier', modifier_name)
		
            Bridge_MO.setAttribute('id', id_name)
        i = i + 1
if mo_name == "VlanPort":
    VlanPort_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
    for VlanPort_MO in VlanPort_list_MO:
        if i ==1:
            VlanPort_MO.setAttribute('modifier', modifier_name)
		
            VlanPort_MO.setAttribute('id', id_name)
        i = i + 1
if mo_name == "Paging":
    Paging_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
    for Paging_MO in Paging_list_MO:
        if i ==1:
            Paging_MO.setAttribute('modifier', modifier_name)
		
            Paging_MO.setAttribute('id', id_name)
        i = i + 1


if mo_name == "GeranCellRelation":
    utrancell_list =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")[0]
    utrancell_list_MO = utrancell_list.getElementsByTagName("xn:VsDataContainer")[0].getElementsByTagName("xn:VsDataContainer")[0].getElementsByTagName("xn:VsDataContainer")
    for utrancell_MO in utrancell_list_MO:
        utrancell_MO.setAttribute('modifier', modifier_name)

if mo_name == "NbapCommon":
    utrancell_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
    for utrancell_MO in utrancell_list_MO:
        if i == 1:
            utrancell_MO.setAttribute('modifier', modifier_name)
        i = i + 1
if mo_name == "Aal2PathVccTp":
    utrancell_list =  xmlTree.getElementsByTagName("xn:ManagedElement")[0].getElementsByTagName("xn:VsDataContainer")[0]
    utrancell_list_MO = utrancell_list.getElementsByTagName("xn:VsDataContainer")
    for utrancell_MO in utrancell_list_MO:
        utrancell_MO.setAttribute('modifier', modifier_name)

k = 0
if mo_name == "LocationArea" or mo_name == "ServiceArea" or mo_name == "RoutingArea":
    if mo_name == "ServiceArea" or mo_name == "RoutingArea":
        utrancell_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")[0].getElementsByTagName("xn:VsDataContainer")[0].getElementsByTagName("xn:VsDataContainer")
        for utrancell_MO in utrancell_list_MO:
            utrancell_MO.setAttribute('modifier', modifier_name)
        utrancell_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")[0].getElementsByTagName("xn:VsDataContainer")
        utrancell_list_MO[0].attributes["id"].value = "1"
        utrancell_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
        utrancell_list_MO[0].attributes["id"].value = "501"
    if mo_name == "LocationArea":
        utrancell_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")[0].getElementsByTagName("xn:VsDataContainer")
        for utrancell_MO in utrancell_list_MO:
	    if k >= 1:
                utrancell_MO.setAttribute('modifier', modifier_name)
	    k = k + 1
        utrancell_list_MO =  xmlTree.getElementsByTagName(first_tag)[0].getElementsByTagName("xn:VsDataContainer")
        utrancell_list_MO[1].attributes["id"].value = "501"

with open(modified_file, 'w') as xml_file:
    DomTree.writexml(xml_file)


print("Success")



