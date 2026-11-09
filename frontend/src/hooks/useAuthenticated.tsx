import { useState, useEffect } from 'react';
import { ApiBridge, JWT_LOCAL_STORAGE_KEY } from '../bridge/api_bridge';
import { local_storage_listen } from '../bridge/local_storage';

export default function useAuthenticated() {
  const [authenticated, set_authenticated] = useState<boolean>(false);

  useEffect(() => {
    let active = true;

    const syncAuth = async () => {
      if (typeof window === 'undefined') {
        set_authenticated(false);
        return;
      }

      const isAuthenticated = await ApiBridge.initialize();
      if (active) {
        set_authenticated(isAuthenticated);
      }
    };

    void syncAuth();

    local_storage_listen((key) => {
      if (key === JWT_LOCAL_STORAGE_KEY) {
        void syncAuth();
      }
    });

    return () => {
      active = false;
    };
  }, []);

  return { authenticated };
}