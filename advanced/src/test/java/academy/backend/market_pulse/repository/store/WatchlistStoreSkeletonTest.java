package academy.backend.market_pulse.repository.store;

import academy.backend.market_pulse.repository.store.impl.WatchlistStoreSkeleton;

class WatchlistStoreSkeletonTest extends WatchlistStoreContractTest {

    @Override
    protected WatchlistStore createStore() {
        return new WatchlistStoreSkeleton();
    }
}
