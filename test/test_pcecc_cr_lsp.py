""""Test case for PCECC CR LSP demo."""
# Copyright (c) 2016 Huawei Technologies Co., Ltd. and others.  All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html

__author__ = "Sojan Koshy"
__copyright__ = "Copyright(c) 2016 Huawei Technologies Co., Ltd."
__license__ = "Eclipse Public License v1.0"
__email__ = "sojan.koshy@huawei.com"

import time

from test.library.odl import Odl
from test.library.svrp import Router
from test.variables import variables as v


def setup_module(module):
    """Setup connections before test execution."""
    print "\nSetup"
    global odl, rt1, rt2, rt3
    odl = Odl(v.base_url)
    rt1 = Router('RT1', v.rt1_telnet_ip)
    rt2 = Router('RT2', v.rt2_telnet_ip)
    rt3 = Router('RT3', v.rt3_telnet_ip)

def teardown_module(module):
    """Tear down connections and pcep operations after test execution."""
    print "\nTear down"
    odl.clean_up()
    rt1.clean_up()
    rt2.clean_up()
    rt3.clean_up()

def test_pcecc_cr_lsp():
    """Test PCECC CR LSP."""
    print "Test PCECC CR LSP"

    # Create PCEP session with all PCCs
    config_pce_on_ingress()
    verify_ingress_in_pcep_topology()
    config_pce_on_transit()
    verify_transit_in_pcep_topology()
    config_pce_on_egress()
    verify_egress_in_pcep_topology()

    # Initiate PCECC CR-LSP without the path-setup-type
    add_lsp()

    # Download labels along the path
    download_labels_on_ingress()
    download_labels_on_transit()
    download_labels_on_egress()

    # Send PcUpdate Message to ingress
    update_lsp()

    # Verify LSP ping
    verify_lsp_ping()

    # Remove labels
    remove_labels_on_ingress()
    remove_labels_on_transit()
    remove_labels_on_egress()

    # Remove LSP
    remove_lsp()


def config_pce_on_ingress():
    """Configure PCE on ingress router and verify the pcep session."""
    params = {'node_id': v.rt1_node_id,
              'pce_server_ip': v.odl_pce_server_ip,
              'intf': v.rt1_to_rt2_intf,
              'ip': v.rt1_to_rt2_ip}
    rt1.set_basic_pce(params)
    time.sleep(1)
    assert rt1.check_pce_up(params)

def verify_ingress_in_pcep_topology():
    """Verify ingress router in the pcep topology in ODL."""
    params = {'node_id': v.rt1_node_id}
    status, resp = odl.get_pcep_topology()
    assert status == 200
    assert resp['topology'][0]['node'][0]["node-id"] == "pcc://" + params['node_id']

def config_pce_on_transit():
    """Configure PCE on egress router and verify the pcep session."""
    params = {'node_id': v.rt2_node_id,
              'pce_server_ip': v.odl_pce_server_ip,
              'intf': v.rt2_to_rt1_intf,
              'ip': v.rt2_to_rt1_ip,
              'intf2': v.rt2_to_rt3_intf,
              'ip2': v.rt2_to_rt3_ip}
    rt2.set_basic_pce(params)
    time.sleep(1)
    assert rt2.check_pce_up(params)

def verify_transit_in_pcep_topology():
    """Verify egress router in the pcep topology in ODL."""
    params = {'node_id': v.rt2_node_id}
    status, resp = odl.get_pcep_topology()
    assert status == 200
    assert resp['topology'][0]['node'][1]["node-id"] == "pcc://" + params['node_id']

def config_pce_on_egress():
    """Configure PCE on egress router and verify the pcep session."""
    params = {'node_id': v.rt3_node_id,
              'pce_server_ip': v.odl_pce_server_ip,
              'intf': v.rt3_to_rt2_intf,
              'ip': v.rt3_to_rt2_ip}
    rt3.set_basic_pce(params)
    time.sleep(1)
    assert rt3.check_pce_up(params)

def verify_egress_in_pcep_topology():
    """Verify egress router in the pcep topology in ODL."""
    params = {'node_id': v.rt3_node_id}
    status, resp = odl.get_pcep_topology()
    assert status == 200
    assert resp['topology'][0]['node'][2]["node-id"] == "pcc://" + params['node_id']

def add_lsp():
    """Add an LSP to ingress router."""
    params = {'node_id': v.rt1_node_id,
              'source': v.rt1_node_id,
              'destination': v.rt3_node_id,
              }
    status, resp = odl.post_add_lsp(params)
    assert status == 200
    assert resp['output'] == {}

def download_labels_on_ingress():
    """Add label to ingress router."""
    params = {'node_id': v.rt1_node_id,
              'out_label': v.rt1_out_label}
    status, resp = odl.post_add_label(params)
    assert status == 200
    assert resp['output'] == {}

def download_labels_on_transit():
    """Add label to transit router."""
    params = {'node_id': v.rt2_node_id,
              'in_label': v.rt2_in_label,
              'out_label': v.rt2_out_label}
    status, resp = odl.post_add_label(params)
    assert status == 200
    assert resp['output'] == {}

def download_labels_on_egress():
    """Add label to egress router."""
    params = {'node_id': v.rt3_node_id,
              'in_label': v.rt3_in_label}
    status, resp = odl.post_add_label(params)
    assert status == 200
    assert resp['output'] == {}

def remove_lsp():
    """Remove the LSP from ingress router"""
    params = {'node_id': v.rt1_node_id}
    status, resp = odl.post_remove_lsp(params)
    assert status == 200
    assert resp['output'] == {}