package com.group16b.DomainLayer.VirtualQueue;


public class VirtualQueueImp  implements IVirtualQueueRepository{
    
    private final static VirtualQueueImp instance = new VirtualQueueImp();

	private VirtualQueueImp() {
	}

	public static VirtualQueueImp getInstance() {
		return instance;
	}

	@Override
	public boolean isUserPassedQueue(int userId, int eventId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isUserPassedQueue'");
	}

	@Override
	public VirtualQueue findVirtualQueueById(long id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findVirtualQueueById'");
	}

	@Override
	public void saveVirtualQueue(VirtualQueue virtualQueue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'saveVirtualQueue'");
	}

	@Override
	public void addVirtualQueue(VirtualQueue virtualQueue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'addVirtualQueue'");
	}
}
