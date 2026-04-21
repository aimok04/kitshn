module Fastlane
  module Actions
    class RetrieveVersionAction < Action
      def self.run(params)
        version = File.read("build.gradle.kts").match(/val kitshnVersionName by extra\("([^"]+)"\)/)[1]
        UI.message("Found kitshn version: #{version}")
        return version
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Retrieving current kitshn version from build.gradle.kts'
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
