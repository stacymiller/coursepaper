from random_subtree import ImitatedAsset

__author__ = 'stacymiller'

M = 5  # width of mesh
n = 10  # length of mesh, # of exercise opportunities

def generate_mesh(initial_price, n, M, asset_state_generator):
    initial_state = ImitatedAsset(initial_price, 0)
    states_on_previous_step = set(initial_state)
    for i in xrange(n):
        new_states = set(asset_state_generator(n) for _ in xrange(M))  # set of no more than M different asset states
        for state in states_on_previous_step:
            state.add_child(new_states)
    return initial_state

def diffusion_mesh_density(n):
    pass

def jump_diffusion_mesh_density():
    pass