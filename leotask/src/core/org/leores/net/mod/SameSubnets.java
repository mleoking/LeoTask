package org.leores.net.mod;

import org.leores.net.Network;
import org.leores.net.Networks;

public class SameSubnets extends Model {
	protected int nSubnets;
	protected Model netModel;

	public SameSubnets(int nSubnets, Model netModel) {
		this.nSubnets = nSubnets;
		this.netModel = netModel;
	}

	public Networks genNetworks(Networks nets) {
		Networks rtn = nets;
		if (rtn == null) {
			rtn = newNetworks(null);
		}
		for (int i = 0; i < nSubnets; i++) {
			Network net = netModel.genNetwork(null);
			rtn.push(net);
		}
		return rtn;
	}

	@Override
	public Network genNetwork(Network net) {
		return null;
	}

}
