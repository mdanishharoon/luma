{
  description = "Dev shell for Compielr Construction project";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs, ...}: let
    pkgs = nixpkgs.legacyPackages."x86_64-linux";
  in {
    devShells.x86_64-linux.default = pkgs.mkShell {
      packages = with pkgs; [
        texliveFull  # Full TeX Live distribution for LaTeX typesetting (reports and documentation)
        jdk23        # OpenJDK 23 for Java development
        maven        # Apache Maven for managing Java project dependencies and builds
        graphviz     # Graphviz for generating graphs for the state diagrams
      ];
  
      shellHook = ''
          echo "Welcome to the Compiler Construction development shell!"
        
          echo "TeX Live is available for LaTeX document processing."
          echo "TeX Live version: $(tex --version | head -n 1)"
          echo "OpenJDK and Maven are available for running java dependencies."
          echo "OpenJDK version: $(java --version | head -n 1)"
          echo "Maven version: $(mvn --version | head -n 1)"
          echo "Graphviz is available for generating graphs for the state diagrams."
          echo "Graphviz version: $(dot -V)"
      '';
    };
  };
}
