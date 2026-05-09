package com.group16b.DomainLayer.VirtualQueue;


public class VirtualQueueImp  implements IVirtualQueueRepository{
    
    private final static VirtualQueueImp instance = new VirtualQueueImp();

	private VirtualQueueImp() {
	}

	public static VirtualQueueImp getInstance() {
		return instance;
	}

	@Override
	public boolean isUserPassedQueue(int userId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isUserPassedQueue'");
	}
}
